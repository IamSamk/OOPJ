package com.ticketing.event.controller;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.EventSummary;
import com.ticketing.common.dto.SeatView;
import com.ticketing.common.model.SeatStatus;
import com.ticketing.event.api.CreateEventRequest;
import com.ticketing.event.api.SeatActionRequest;
import com.ticketing.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    public Mono<ResponseEntity<ApiResponse<EventSummary>>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventService.createEvent(request)
                .map(summary -> ResponseEntity.ok(ApiResponse.success("Event created", summary, null)));
    }

    @GetMapping("/events")
    public Mono<ResponseEntity<ApiResponse<List<EventSummary>>>> listEvents() {
        return eventService.listEvents()
                .map(events -> ResponseEntity.ok(ApiResponse.success("Events fetched", events, null)));
    }

    @GetMapping("/events/{id}")
    public Mono<ResponseEntity<ApiResponse<EventSummary>>> getEvent(@PathVariable UUID id) {
        return eventService.getEvent(id)
                .map(event -> ResponseEntity.ok(ApiResponse.success("Event fetched", event, null)));
    }

    @GetMapping("/events/{id}/seats")
    public Mono<ResponseEntity<ApiResponse<List<SeatView>>>> getSeats(@PathVariable UUID id) {
        return eventService.getSeatMap(id)
                .map(seats -> ResponseEntity.ok(ApiResponse.success("Seat map fetched", seats, null)));
    }

    @PostMapping("/events/{id}/seats/hold")
    public Mono<ResponseEntity<ApiResponse<Void>>> holdSeats(@PathVariable UUID id, @Valid @RequestBody SeatActionRequest request) {
        return eventService.updateSeatStatuses(id, request.seatIds(), SeatStatus.HELD)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("Seats held", null, null)));
    }

    @PostMapping("/events/{id}/seats/reserve")
    public Mono<ResponseEntity<ApiResponse<Void>>> reserveSeats(@PathVariable UUID id, @Valid @RequestBody SeatActionRequest request) {
        return eventService.updateSeatStatuses(id, request.seatIds(), SeatStatus.RESERVED)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("Seats reserved", null, null)));
    }

    @PostMapping("/events/{id}/seats/release")
    public Mono<ResponseEntity<ApiResponse<Void>>> releaseSeats(@PathVariable UUID id, @Valid @RequestBody SeatActionRequest request) {
        return eventService.updateSeatStatuses(id, request.seatIds(), SeatStatus.AVAILABLE)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("Seats released", null, null)));
    }

    @GetMapping(value = "/events/{id}/seats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<SeatView>>> streamSeats(@PathVariable UUID id) {
        return Flux.interval(Duration.ofSeconds(3))
                .flatMap(tick -> eventService.getSeatMap(id)
                        .map(seats -> ServerSentEvent.<List<SeatView>>builder().event("seat-map").data(seats).build()));
    }
}
