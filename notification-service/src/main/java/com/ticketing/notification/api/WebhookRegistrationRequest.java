package com.ticketing.notification.api;

import jakarta.validation.constraints.NotBlank;

public record WebhookRegistrationRequest(@NotBlank String callbackUrl,
                                         @NotBlank String eventType,
                                         String secret) {
}
