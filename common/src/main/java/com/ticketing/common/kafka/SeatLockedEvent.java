package com.ticketing.common.kafka;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeatLockedEvent(UUID bookingId,
                              UUID eventId,
                              List<UUID> seatIds,
                              Instant expiresAt) {
}
