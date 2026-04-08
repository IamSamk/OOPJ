package com.ticketing.booking.service;

import com.ticketing.booking.api.BookingView;
import com.ticketing.booking.domain.BookingEntity;
import com.ticketing.booking.repo.BookingRepository;
import com.ticketing.common.dto.BookingRequest;
import com.ticketing.common.dto.BookingResponse;
import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.PaymentIntentRequest;
import com.ticketing.common.dto.PaymentIntentResponse;
import com.ticketing.common.dto.PricingQuote;
import com.ticketing.common.dto.SeatView;
import com.ticketing.common.exception.NotFoundException;
import com.ticketing.common.kafka.BookingCreatedEvent;
import com.ticketing.common.kafka.SeatLockedEvent;
import com.ticketing.common.model.BookingStatus;
import com.ticketing.booking.client.EventClient;
import com.ticketing.booking.client.PaymentClient;
import com.ticketing.booking.client.PricingClient;
import com.ticketing.booking.config.BookingProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisSeatLockService seatLockService;
    private final EventClient eventClient;
    private final PricingClient pricingClient;
    private final PaymentClient paymentClient;
    private final BookingProperties bookingProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingService(BookingRepository bookingRepository,
                          RedisSeatLockService seatLockService,
                          EventClient eventClient,
                          PricingClient pricingClient,
                          PaymentClient paymentClient,
                          BookingProperties bookingProperties,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.seatLockService = seatLockService;
        this.eventClient = eventClient;
        this.pricingClient = pricingClient;
        this.paymentClient = paymentClient;
        this.bookingProperties = bookingProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<BookingResponse> createBooking(UUID userId, BookingRequest request) {
        return eventClient.getEvent(request.eventId())
                .zipWith(eventClient.getSeats(request.eventId()))
                .flatMap(tuple -> createBookingInternal(userId, request, tuple.getT1(), tuple.getT2()));
    }

    public Mono<BookingResponse> getBooking(UUID bookingId) {
        return Mono.fromCallable(() -> bookingRepository.findById(bookingId)
                        .map(this::toResponse)
                        .orElseThrow(() -> new NotFoundException("Booking not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> handlePaymentCompleted(UUID bookingId, UUID paymentId, boolean success) {
        return Mono.fromCallable(() -> {
                    BookingEntity booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> new NotFoundException("Booking not found"));
                    booking.setPaymentId(paymentId);
                    if (success) {
                        booking.setStatus(BookingStatus.CONFIRMED);
                        bookingRepository.save(booking);
                        eventClient.reserveSeats(booking.getEventId(), booking.getSeatIds()).subscribe();
                    } else {
                        booking.setStatus(BookingStatus.FAILED);
                        bookingRepository.save(booking);
                        seatLockService.releaseSeats(booking.getEventId(), booking.getSeatIds()).subscribe();
                        eventClient.releaseSeats(booking.getEventId(), booking.getSeatIds()).subscribe();
                    }
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<BookingResponse> createBookingInternal(UUID userId, BookingRequest request, EventSummary event, List<SeatView> seats) {
        validateSeats(request, seats);
        Map<String, Long> seatCounts = seats.stream().collect(Collectors.groupingBy(SeatView::seatType, Collectors.counting()));
        String bookingRef = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Duration holdTtl = Duration.ofMinutes(bookingProperties.getSeatHoldMinutes());

        return seatLockService.lockSeats(request.eventId(), request.seatIds(), bookingRef, holdTtl)
                .then(calculateAmount(event, seatCounts)
                        .flatMap(amount -> persistBooking(userId, request, amount, bookingRef, holdTtl)
                                .flatMap(savedBooking -> createPaymentIntent(savedBooking, amount)
                                        .map(paymentResponse -> finalizeBooking(savedBooking, paymentResponse))))) ;
    }

    private Mono<BigDecimal> calculateAmount(EventSummary event, Map<String, Long> seatCounts) {
        return reactor.core.publisher.Flux.fromIterable(seatCounts.entrySet())
                .flatMap(entry -> pricingClient.calculate(event.id(), entry.getKey())
                        .map(quote -> quote.calculatedPrice().multiply(BigDecimal.valueOf(entry.getValue()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<BookingEntity> persistBooking(UUID userId,
                                               BookingRequest request,
                                               BigDecimal amount,
                                               String bookingRef,
                                               Duration holdTtl) {
        return Mono.fromCallable(() -> {
                    BookingEntity booking = new BookingEntity();
                    booking.setBookingRef(bookingRef);
                    booking.setUserId(userId);
                    booking.setEventId(request.eventId());
                    booking.setSeatIds(request.seatIds());
                    booking.setAmount(amount);
                    booking.setStatus(BookingStatus.PENDING_PAYMENT);
                    booking.setExpiresAt(Instant.now().plus(holdTtl));
                    BookingEntity saved = bookingRepository.save(booking);
                    kafkaTemplate.send("booking.created", saved.getId().toString(), new BookingCreatedEvent(saved.getId(), saved.getBookingRef(), userId, request.eventId(), request.seatIds(), amount, Instant.now()));
                    kafkaTemplate.send("seat.locked", saved.getId().toString(), new SeatLockedEvent(saved.getId(), request.eventId(), request.seatIds(), saved.getExpiresAt()));
                    return saved;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<PaymentIntentResponse> createPaymentIntent(BookingEntity booking, BigDecimal amount) {
        PaymentIntentRequest paymentIntentRequest = new PaymentIntentRequest(booking.getId(), amount, "INR", "MOCK_STRIPE");
        return paymentClient.createIntent(paymentIntentRequest);
    }

    private BookingResponse finalizeBooking(BookingEntity booking, PaymentIntentResponse paymentResponse) {
        booking.setPaymentId(paymentResponse.paymentId());
        BookingEntity saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private void validateSeats(BookingRequest request, List<SeatView> seats) {
        if (seats.size() != request.seatIds().size()) {
            throw new NotFoundException("One or more seats are invalid");
        }
        boolean hasUnavailable = seats.stream().anyMatch(seat -> !"AVAILABLE".equalsIgnoreCase(seat.status()));
        if (hasUnavailable) {
            throw new IllegalStateException("One or more seats are not available");
        }
    }

    private BookingResponse toResponse(BookingEntity booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingRef(),
                booking.getEventId(),
                booking.getSeatIds(),
                booking.getStatus(),
                booking.getAmount(),
                booking.getPaymentId(),
                booking.getExpiresAt()
        );
    }
}
