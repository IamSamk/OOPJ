package com.ticketing.event.service;

import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.SeatView;
import com.ticketing.common.exception.NotFoundException;
import com.ticketing.common.model.SeatStatus;
import com.ticketing.event.api.CreateEventRequest;
import com.ticketing.event.domain.EventEntity;
import com.ticketing.event.domain.SeatEntity;
import com.ticketing.event.repo.EventRepository;
import com.ticketing.event.repo.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final SeatMapCacheService cacheService;

    public EventService(EventRepository eventRepository, SeatRepository seatRepository, SeatMapCacheService cacheService) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.cacheService = cacheService;
    }

    public Mono<EventSummary> createEvent(CreateEventRequest request) {
        return Mono.fromCallable(() -> {
                    EventEntity event = new EventEntity();
                    event.setName(request.name());
                    event.setVenue(request.venue());
                    event.setDescription(request.description());
                    event.setEventTime(request.eventTime());
                    event.setBasePrice(request.basePrice());
                    int totalSeats = request.seatDistribution().values().stream().mapToInt(Integer::intValue).sum();
                    event.setTotalSeats(totalSeats);
                    event.setRemainingSeats(totalSeats);
                    EventEntity savedEvent = eventRepository.save(event);
                    List<SeatEntity> seats = new ArrayList<>();
                    request.seatDistribution().forEach((seatType, count) -> {
                        for (int index = 1; index <= count; index++) {
                            SeatEntity seat = new SeatEntity();
                            seat.setEvent(savedEvent);
                            seat.setSeatType(seatType);
                            seat.setSeatNumber(seatType.substring(0, Math.min(3, seatType.length())).toUpperCase() + "-" + index);
                            seat.setPrice(savedEvent.getBasePrice().multiply(multiplierFor(seatType)));
                            seat.setStatus(SeatStatus.AVAILABLE.name());
                            seats.add(seat);
                        }
                    });
                    seatRepository.saveAll(seats);
                    cacheService.evict(savedEvent.getId()).subscribe();
                    return toSummary(savedEvent);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<EventSummary>> listEvents() {
        return Mono.fromCallable(() -> eventRepository.findAll().stream().map(this::toSummary).sorted(Comparator.comparing(EventSummary::eventTime)).toList())
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<EventSummary> getEvent(UUID eventId) {
        return Mono.fromCallable(() -> eventRepository.findById(eventId)
                        .map(this::toSummary)
                        .orElseThrow(() -> new NotFoundException("Event not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<SeatView>> getSeatMap(UUID eventId) {
        return cacheService.get(eventId)
                .switchIfEmpty(Mono.fromCallable(() -> seatRepository.findByEvent_Id(eventId).stream().map(this::toSeatView).toList())
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(seatViews -> cacheService.put(eventId, seatViews).thenReturn(seatViews)));
    }

    @Transactional
    public Mono<Void> updateSeatStatuses(UUID eventId, List<UUID> seatIds, SeatStatus status) {
        return Mono.fromRunnable(() -> {
                    List<SeatEntity> seats = seatRepository.findByEvent_IdAndIdIn(eventId, seatIds);
                    if (seats.size() != seatIds.size()) {
                        throw new NotFoundException("One or more seats were not found");
                    }
                    seats.forEach(seat -> seat.setStatus(status.name()));
                    seatRepository.saveAll(seats);
                    EventEntity event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new NotFoundException("Event not found"));
                    long heldOrReserved = seatRepository.findByEvent_Id(eventId).stream()
                            .filter(seat -> !SeatStatus.AVAILABLE.name().equals(seat.getStatus()))
                            .count();
                    event.setRemainingSeats((int) Math.max(0, event.getTotalSeats() - heldOrReserved));
                    eventRepository.save(event);
                    cacheService.evict(eventId).subscribe();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private EventSummary toSummary(EventEntity event) {
        return new EventSummary(
                event.getId(),
                event.getName(),
                event.getVenue(),
                event.getEventTime(),
                event.getBasePrice(),
                event.getTotalSeats(),
                event.getRemainingSeats(),
                event.getStatus()
        );
    }

    private SeatView toSeatView(SeatEntity seat) {
        return new SeatView(
                seat.getId(),
                seat.getEvent().getId(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                seat.getPrice(),
                seat.getStatus(),
                seat.getVersion()
        );
    }

    private BigDecimal multiplierFor(String seatType) {
        return switch (seatType.toUpperCase()) {
            case "VIP" -> BigDecimal.valueOf(1.75d);
            case "PREMIUM" -> BigDecimal.valueOf(1.35d);
            default -> BigDecimal.ONE;
        };
    }
}
