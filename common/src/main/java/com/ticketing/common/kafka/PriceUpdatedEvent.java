package com.ticketing.common.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceUpdatedEvent(UUID eventId,
                                String seatType,
                                BigDecimal price,
                                double demandMultiplier,
                                double timeFactor,
                                double scarcityFactor,
                                double bookingVelocity,
                                Instant updatedAt) {
}
