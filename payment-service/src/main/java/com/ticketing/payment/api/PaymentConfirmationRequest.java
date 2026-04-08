package com.ticketing.payment.api;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmationRequest(boolean success,
                                        @NotBlank String transactionRef) {
}
