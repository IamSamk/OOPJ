package com.ticketing.notification.kafka;

import com.ticketing.common.kafka.PriceUpdatedEvent;
import com.ticketing.notification.service.WebhookDispatcherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PriceEventListener {

    private final WebhookDispatcherService dispatcherService;

    public PriceEventListener(WebhookDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @KafkaListener(topics = "price.updated", groupId = "notification-service")
    public void handle(PriceUpdatedEvent event) {
        dispatcherService.dispatch("price.updated", event);
    }
}
