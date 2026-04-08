package com.ticketing.pricing.service;

import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.PricingQuote;
import com.ticketing.common.dto.SeatView;
import com.ticketing.common.kafka.PriceUpdatedEvent;
import com.ticketing.common.model.SeatStatus;
import com.ticketing.pricing.client.EventClient;
import com.ticketing.pricing.domain.PricingHistory;
import com.ticketing.pricing.domain.PricingRule;
import com.ticketing.pricing.repo.PricingHistoryRepository;
import com.ticketing.pricing.repo.PricingRuleRepository;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PricingService {

    private final EventClient eventClient;
    private final PricingRuleRepository pricingRuleRepository;
    private final PricingHistoryRepository pricingHistoryRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, PriceUpdatedEvent> kafkaTemplate;

    public PricingService(EventClient eventClient,
                          PricingRuleRepository pricingRuleRepository,
                          PricingHistoryRepository pricingHistoryRepository,
                          ReactiveStringRedisTemplate redisTemplate,
                          KafkaTemplate<String, PriceUpdatedEvent> kafkaTemplate) {
        this.eventClient = eventClient;
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingHistoryRepository = pricingHistoryRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<PricingQuote> calculate(UUID eventId, String seatType) {
        return Mono.zip(eventClient.getEvent(eventId), activeViews(eventId, seatType), remainingSeats(eventId, seatType))
                .flatMap(tuple -> Mono.fromCallable(() -> persistQuote(tuple.getT1(), seatType, tuple.getT2(), tuple.getT3()))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<List<PricingQuote>> latestForEvent(UUID eventId) {
        return Mono.fromCallable(() -> pricingHistoryRepository.findTop20ByEventIdOrderByCreatedAtDesc(eventId).stream()
                        .map(history -> new PricingQuote(
                                history.getEventId(),
                                history.getSeatType(),
                                null,
                                history.getCalculatedPrice(),
                                history.getDemandMultiplier(),
                                history.getTimeFactor(),
                                history.getScarcityFactor(),
                                history.getBookingVelocity(),
                                "INR",
                                history.getCreatedAt()))
                        .toList())
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PricingQuote> registerView(UUID eventId, String seatType) {
        return incrementViewCounter(eventId, seatType).then(calculate(eventId, seatType));
    }

    public Flux<PricingQuote> stream(UUID eventId, String seatType) {
        return Flux.interval(Duration.ofSeconds(5)).flatMap(tick -> calculate(eventId, seatType));
    }

    private PricingQuote persistQuote(EventSummary event, String seatType, long activeViews, long remainingSeats) {
        BigDecimal basePrice = event.basePrice();
        PricingRule rule = pricingRuleRepository.findBySeatTypeIgnoreCaseAndActiveTrue(seatType).orElseGet(() -> defaultRule(seatType));
        double demandMultiplier = Math.min(rule.getMaxMultiplier().doubleValue(), 1.0d + (activeViews / 12.0d));
        double timeFactor = calculateTimeFactor(event.eventTime());
        double scarcityFactor = remainingSeats <= 0 ? 3.0d : 1.0d + (1.0d - ((double) remainingSeats / Math.max(1, event.totalSeats()))) * 2.0d;
        double bookingVelocity = calculateVelocity(event.id());
        BigDecimal price = basePrice
                .multiply(BigDecimal.valueOf(demandMultiplier))
                .multiply(BigDecimal.valueOf(timeFactor))
                .multiply(BigDecimal.valueOf(scarcityFactor))
                .multiply(BigDecimal.valueOf(1.0d + bookingVelocity / 10.0d))
                .setScale(2, RoundingMode.HALF_UP);

        PricingHistory history = new PricingHistory();
        history.setEventId(event.id());
        history.setSeatType(seatType);
        history.setCalculatedPrice(price);
        history.setDemandMultiplier(demandMultiplier);
        history.setTimeFactor(timeFactor);
        history.setScarcityFactor(scarcityFactor);
        history.setBookingVelocity(bookingVelocity);
        history.setRemainingSeats((int) remainingSeats);
        history.setActiveViews((int) activeViews);
        history.setCreatedAt(Instant.now());
        pricingHistoryRepository.save(history);

        PriceUpdatedEvent eventMessage = new PriceUpdatedEvent(event.id(), seatType, price, demandMultiplier, timeFactor, scarcityFactor, bookingVelocity, Instant.now());
        kafkaTemplate.send("price.updated", event.id().toString(), eventMessage);

        return new PricingQuote(event.id(), seatType, basePrice, price, demandMultiplier, timeFactor, scarcityFactor, bookingVelocity, "INR", Instant.now());
    }

    private Mono<Long> activeViews(UUID eventId, String seatType) {
        return redisTemplate.opsForValue().get(counterKey(eventId, seatType))
                .map(Long::valueOf)
                .defaultIfEmpty(0L);
    }

    private Mono<Long> remainingSeats(UUID eventId, String seatType) {
        return eventClient.getSeats(eventId)
                .map(seats -> seats.stream().filter(seat -> seatType.equalsIgnoreCase(seat.seatType()) && SeatStatus.AVAILABLE.name().equals(seat.status())).count())
                .defaultIfEmpty(0L);
    }

    private Mono<Boolean> incrementViewCounter(UUID eventId, String seatType) {
        return redisTemplate.opsForValue().increment(counterKey(eventId, seatType))
                .map(count -> true)
                .flatMap(result -> redisTemplate.expire(counterKey(eventId, seatType), Duration.ofHours(4)).thenReturn(result));
    }

    private double calculateTimeFactor(Instant eventTime) {
        long daysToEvent = Math.max(0, ChronoUnit.DAYS.between(Instant.now(), eventTime));
        return 1.0d + Math.max(0, 30 - daysToEvent) / 25.0d;
    }

    private double calculateVelocity(UUID eventId) {
        List<PricingHistory> history = pricingHistoryRepository.findTop20ByEventIdOrderByCreatedAtDesc(eventId);
        return Math.min(3.0d, history.size() / 10.0d);
    }

    private PricingRule defaultRule(String seatType) {
        PricingRule rule = new PricingRule();
        rule.setSeatType(seatType);
        rule.setMinMultiplier(BigDecimal.ONE);
        rule.setMaxMultiplier(BigDecimal.valueOf(3.0d));
        return rule;
    }

    private String counterKey(UUID eventId, String seatType) {
        return "pricing:view:" + eventId + ":" + seatType.toUpperCase();
    }
}
