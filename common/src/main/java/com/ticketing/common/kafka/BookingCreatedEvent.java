package com.ticketing.common.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingCreatedEvent(UUID bookingId,
                                  String bookingRef,
                                  UUID userId,
                                  UUID eventId,
                                  List<UUID> seatIds,
                                  BigDecimal amount,
                                  Instant createdAt) {
}
