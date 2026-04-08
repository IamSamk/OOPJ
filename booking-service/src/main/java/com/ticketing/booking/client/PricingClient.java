package com.ticketing.booking.client;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.PricingQuote;
import com.ticketing.booking.config.BookingProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PricingClient {

    private final WebClient webClient;

    public PricingClient(BookingProperties properties) {
        this.webClient = WebClient.builder().baseUrl(properties.getPricingServiceBaseUrl()).build();
    }

    public Mono<PricingQuote> calculate(UUID eventId, String seatType) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/pricing/calculate")
                        .queryParam("eventId", eventId)
                        .queryParam("seatType", seatType)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PricingQuote>>() { })
                .map(ApiResponse::data);
    }
}
