package com.ticketing.common.dto;

import com.ticketing.common.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentIntentResponse(UUID paymentId,
                                    String intentId,
                                    PaymentStatus status,
                                    BigDecimal amount,
                                    String redirectUrl) {
}
