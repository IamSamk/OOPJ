package com.ticketing.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.common.dto.SeatView;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class SeatMapCacheService {

    private static final TypeReference<List<SeatView>> SEAT_LIST_TYPE = new TypeReference<>() { };
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SeatMapCacheService(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<List<SeatView>> get(UUID eventId) {
        return redisTemplate.opsForValue().get(key(eventId))
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, SEAT_LIST_TYPE)));
    }

    public Mono<Boolean> put(UUID eventId, List<SeatView> seatViews) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(seatViews))
                .flatMap(json -> redisTemplate.opsForValue().set(key(eventId), json, Duration.ofMinutes(10)));
    }

    public Mono<Boolean> evict(UUID eventId) {
        return redisTemplate.delete(key(eventId)).map(count -> count > 0);
    }

    private String key(UUID eventId) {
        return "seat-map:" + eventId;
    }
}
