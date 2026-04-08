package com.ticketing.notification.kafka;

import com.ticketing.common.kafka.BookingCreatedEvent;
import com.ticketing.notification.service.WebhookDispatcherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    private final WebhookDispatcherService dispatcherService;

    public BookingEventListener(WebhookDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @KafkaListener(topics = "booking.created", groupId = "notification-service")
    public void handle(BookingCreatedEvent event) {
        dispatcherService.dispatch("booking.created", event);
    }
}
