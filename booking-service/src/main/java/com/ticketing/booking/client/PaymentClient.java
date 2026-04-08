package com.ticketing.booking.client;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.PaymentIntentRequest;
import com.ticketing.common.dto.PaymentIntentResponse;
import com.ticketing.booking.config.BookingProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(BookingProperties properties) {
        this.webClient = WebClient.builder().baseUrl(properties.getPaymentServiceBaseUrl()).build();
    }

    public Mono<PaymentIntentResponse> createIntent(PaymentIntentRequest request) {
        return webClient.post()
                .uri("/payments/intents")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentIntentResponse>>() { })
                .map(ApiResponse::data);
    }
}
