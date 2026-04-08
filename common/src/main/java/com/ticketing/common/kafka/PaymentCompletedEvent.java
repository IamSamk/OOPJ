package com.ticketing.common.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentId,
                                    UUID bookingId,
                                    String status,
                                    String transactionRef,
                                    BigDecimal amount,
                                    Instant completedAt) {
}
