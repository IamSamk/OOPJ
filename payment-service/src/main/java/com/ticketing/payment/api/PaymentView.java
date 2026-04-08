package com.ticketing.payment.api;

import com.ticketing.common.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentView(UUID id,
                          String intentId,
                          UUID bookingId,
                          BigDecimal amount,
                          String currency,
                          String provider,
                          String transactionRef,
                          PaymentStatus status) {
}
