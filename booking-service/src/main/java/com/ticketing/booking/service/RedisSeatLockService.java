package com.ticketing.booking.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class RedisSeatLockService {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisSeatLockService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> lockSeats(UUID eventId, List<UUID> seatIds, String bookingRef, Duration ttl) {
        return Flux.fromIterable(seatIds)
                .concatMap(seatId -> redisTemplate.opsForValue()
                        .setIfAbsent(key(eventId, seatId), bookingRef, ttl)
                        .flatMap(acquired -> acquired ? Mono.just(seatId) : Mono.error(new IllegalStateException("Seat already locked"))))
                .collectList()
                .flatMap(locked -> Mono.just(Boolean.TRUE))
                .onErrorResume(error -> releaseSeats(eventId, seatIds).then(Mono.error(error)));
    }

    public Mono<Long> releaseSeats(UUID eventId, List<UUID> seatIds) {
        return Flux.fromIterable(seatIds)
                .concatMap(seatId -> redisTemplate.delete(key(eventId, seatId)))
                .reduce(0L, Long::sum);
    }

    private String key(UUID eventId, UUID seatId) {
        return "seat-lock:" + eventId + ":" + seatId;
    }
}
