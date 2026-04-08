package com.ticketing.notification.controller;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.notification.api.WebhookRegistrationRequest;
import com.ticketing.notification.api.WebhookRegistrationResponse;
import com.ticketing.notification.service.WebhookDispatcherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final WebhookDispatcherService dispatcherService;

    public WebhookController(WebhookDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<WebhookRegistrationResponse>>> register(@Valid @RequestBody WebhookRegistrationRequest request) {
        return dispatcherService.register(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Webhook registered", response, null)));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<String>>>> list() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Webhook management endpoint", List.of("register", "dispatch via kafka listeners"), null)));
    }
}
