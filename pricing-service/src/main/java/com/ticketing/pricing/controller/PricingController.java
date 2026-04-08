package com.ticketing.pricing.controller;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.PricingQuote;
import com.ticketing.pricing.service.PricingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/pricing/calculate")
    public Mono<ResponseEntity<ApiResponse<PricingQuote>>> calculate(@RequestParam UUID eventId,
                                                                     @RequestParam String seatType) {
        return pricingService.calculate(eventId, seatType)
                .map(quote -> ResponseEntity.ok(ApiResponse.success("Price calculated", quote, null)));
    }

    @GetMapping("/pricing/{eventId}")
    public Mono<ResponseEntity<ApiResponse<List<PricingQuote>>>> latest(@PathVariable UUID eventId) {
        return pricingService.latestForEvent(eventId)
                .map(quotes -> ResponseEntity.ok(ApiResponse.success("Latest pricing fetched", quotes, null)));
    }

    @PostMapping("/pricing/{eventId}/views")
    public Mono<ResponseEntity<ApiResponse<PricingQuote>>> registerView(@PathVariable UUID eventId,
                                                                         @RequestParam String seatType) {
        return pricingService.registerView(eventId, seatType)
                .map(quote -> ResponseEntity.ok(ApiResponse.success("View registered", quote, null)));
    }

    @GetMapping(value = "/pricing/{eventId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<PricingQuote>> stream(@PathVariable UUID eventId,
                                                      @RequestParam String seatType) {
        return pricingService.stream(eventId, seatType)
                .map(quote -> ServerSentEvent.<PricingQuote>builder().event("price.updated").data(quote).build());
    }
}
