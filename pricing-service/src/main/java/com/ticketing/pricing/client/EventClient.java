package com.ticketing.pricing.client;

import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.SeatView;
import com.ticketing.common.exception.NotFoundException;
import com.ticketing.pricing.config.PricingProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class EventClient {

    private final WebClient webClient;

    public EventClient(PricingProperties properties) {
        this.webClient = WebClient.builder().baseUrl(properties.getEventServiceBaseUrl()).build();
    }

    public Mono<EventSummary> getEvent(UUID eventId) {
        return webClient.get()
                .uri("/events/{id}", eventId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<com.ticketing.common.api.ApiResponse<EventSummary>>() { })
                .map(com.ticketing.common.api.ApiResponse::data)
                .switchIfEmpty(Mono.error(new NotFoundException("Event not found")));
    }

    public Mono<List<SeatView>> getSeats(UUID eventId) {
        return webClient.get()
                .uri("/events/{id}/seats", eventId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<com.ticketing.common.api.ApiResponse<List<SeatView>>>() { })
                .map(com.ticketing.common.api.ApiResponse::data);
    }
}
