package com.ticketing.notification.api;

import java.util.UUID;

public record WebhookRegistrationResponse(UUID id, String callbackUrl, String eventType, boolean active) {
}
