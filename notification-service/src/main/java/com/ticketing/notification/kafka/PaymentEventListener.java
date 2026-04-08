package com.ticketing.notification.kafka;

import com.ticketing.common.kafka.PaymentCompletedEvent;
import com.ticketing.notification.service.WebhookDispatcherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final WebhookDispatcherService dispatcherService;

    public PaymentEventListener(WebhookDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-service")
    public void handle(PaymentCompletedEvent event) {
        dispatcherService.dispatch("payment.completed", event);
    }
}
