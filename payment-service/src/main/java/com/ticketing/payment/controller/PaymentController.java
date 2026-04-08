package com.ticketing.payment.controller;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.PaymentIntentRequest;
import com.ticketing.common.dto.PaymentIntentResponse;
import com.ticketing.payment.api.PaymentConfirmationRequest;
import com.ticketing.payment.api.PaymentView;
import com.ticketing.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/intents")
    public Mono<ResponseEntity<ApiResponse<PaymentIntentResponse>>> createIntent(@Valid @RequestBody PaymentIntentRequest request) {
        return paymentService.createIntent(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Payment intent created", response, null)));
    }

    @PostMapping("/{id}/confirm")
    public Mono<ResponseEntity<ApiResponse<PaymentView>>> confirm(@PathVariable UUID id,
                                                                   @Valid @RequestBody PaymentConfirmationRequest request) {
        return paymentService.confirmPayment(id, request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Payment confirmed", response, null)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<PaymentView>>> get(@PathVariable UUID id) {
        return paymentService.getPayment(id)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Payment fetched", response, null)));
    }
}
