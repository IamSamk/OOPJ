package com.ticketing.booking.client;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.SeatView;
import com.ticketing.booking.config.BookingProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class EventClient {

    private final WebClient webClient;

    public EventClient(BookingProperties properties) {
        this.webClient = WebClient.builder().baseUrl(properties.getEventServiceBaseUrl()).build();
    }

    public Mono<EventSummary> getEvent(UUID eventId) {
        return webClient.get()
                .uri("/events/{id}", eventId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<EventSummary>>() { })
                .map(ApiResponse::data);
    }

    public Mono<List<SeatView>> getSeats(UUID eventId) {
        return webClient.get()
                .uri("/events/{id}/seats", eventId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<SeatView>>>() { })
                .map(ApiResponse::data);
    }

    public Mono<Void> holdSeats(UUID eventId, List<UUID> seatIds) {
        return webClient.post()
                .uri("/events/{id}/seats/hold", eventId)
                .bodyValue(seatIds)
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }

    public Mono<Void> reserveSeats(UUID eventId, List<UUID> seatIds) {
        return webClient.post()
                .uri("/events/{id}/seats/reserve", eventId)
                .bodyValue(seatIds)
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }

    public Mono<Void> releaseSeats(UUID eventId, List<UUID> seatIds) {
        return webClient.post()
                .uri("/events/{id}/seats/release", eventId)
                .bodyValue(seatIds)
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }
}
