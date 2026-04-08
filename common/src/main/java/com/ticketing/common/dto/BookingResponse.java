package com.ticketing.common.dto;

import com.ticketing.common.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(UUID bookingId,
                              String bookingRef,
                              UUID eventId,
                              List<UUID> seatIds,
                              BookingStatus status,
                              BigDecimal amount,
                              UUID paymentId,
                              Instant expiresAt) {
}
