package com.ticketing.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PricingQuote(UUID eventId,
                           String seatType,
                           BigDecimal basePrice,
                           BigDecimal calculatedPrice,
                           double demandMultiplier,
                           double timeFactor,
                           double scarcityFactor,
                           double bookingVelocity,
                           String currency,
                           Instant generatedAt) {
}
