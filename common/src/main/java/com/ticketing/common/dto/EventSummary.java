package com.ticketing.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventSummary(UUID id,
                           String name,
                           String venue,
                           Instant eventTime,
                           BigDecimal basePrice,
                           Integer totalSeats,
                           Integer remainingSeats,
                           String status) {
}
