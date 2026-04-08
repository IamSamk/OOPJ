package com.ticketing.booking.kafka;

import com.ticketing.booking.service.BookingService;
import com.ticketing.common.kafka.PaymentCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedListener {

    private final BookingService bookingService;

    public PaymentCompletedListener(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = "payment.completed", groupId = "booking-service")
    public void handle(PaymentCompletedEvent event) {
        bookingService.handlePaymentCompleted(event.bookingId(), event.paymentId(), "SUCCESS".equalsIgnoreCase(event.status())).subscribe();
    }
}
