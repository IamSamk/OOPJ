package com.ticketing.common.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentIntentRequest(@NotNull UUID bookingId,
                                   @NotNull BigDecimal amount,
                                   String currency,
                                   String provider) {
}
