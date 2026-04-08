package com.ticketing.booking.controller;

import com.ticketing.booking.service.BookingService;
import com.ticketing.common.api.ApiResponse;
import com.ticketing.common.dto.BookingRequest;
import com.ticketing.common.dto.BookingResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<BookingResponse>>> create(@Valid @RequestBody BookingRequest request,
                                                                     Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return bookingService.createBooking(userId, request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Booking created", response, null)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BookingResponse>>> getById(@PathVariable UUID id) {
        return bookingService.getBooking(id)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Booking fetched", response, null)));
    }
}
