package com.ticketing.event.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record CreateEventRequest(@NotBlank String name,
                                 @NotBlank String venue,
                                 String description,
                                 @NotNull Instant eventTime,
                                 @NotNull BigDecimal basePrice,
                                 @NotEmpty Map<String, Integer> seatDistribution) {
}
