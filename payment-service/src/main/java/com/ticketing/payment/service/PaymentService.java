package com.ticketing.payment.service;

import com.ticketing.common.dto.PaymentIntentRequest;
import com.ticketing.common.dto.PaymentIntentResponse;
import com.ticketing.common.kafka.PaymentCompletedEvent;
import com.ticketing.common.model.PaymentStatus;
import com.ticketing.payment.api.PaymentConfirmationRequest;
import com.ticketing.payment.api.PaymentView;
import com.ticketing.payment.domain.PaymentEntity;
import com.ticketing.payment.repo.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<PaymentIntentResponse> createIntent(PaymentIntentRequest request) {
        return Mono.fromCallable(() -> {
                    PaymentEntity payment = new PaymentEntity();
                    payment.setIntentId("PI-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
                    payment.setBookingId(request.bookingId());
                    payment.setAmount(request.amount());
                    payment.setCurrency(request.currency() == null ? "INR" : request.currency());
                    payment.setProvider(request.provider() == null ? "MOCK_STRIPE" : request.provider());
                    payment.setStatus(PaymentStatus.INITIATED);
                    PaymentEntity saved = paymentRepository.save(payment);
                    return new PaymentIntentResponse(saved.getId(), saved.getIntentId(), saved.getStatus(), saved.getAmount(), "https://payments.local/intent/" + saved.getIntentId());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentView> confirmPayment(UUID paymentId, PaymentConfirmationRequest request) {
        return Mono.fromCallable(() -> {
                    PaymentEntity payment = paymentRepository.findById(paymentId)
                            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
                    payment.setTransactionRef(request.transactionRef());
                    payment.setStatus(request.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILURE);
                    PaymentEntity saved = paymentRepository.save(payment);
                    kafkaTemplate.send("payment.completed", saved.getBookingId().toString(), new PaymentCompletedEvent(saved.getId(), saved.getBookingId(), saved.getStatus().name(), saved.getTransactionRef(), saved.getAmount(), Instant.now()));
                    return toView(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentView> getPayment(UUID paymentId) {
        return Mono.fromCallable(() -> paymentRepository.findById(paymentId)
                        .map(this::toView)
                        .orElseThrow(() -> new IllegalArgumentException("Payment not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private PaymentView toView(PaymentEntity payment) {
        return new PaymentView(
                payment.getId(),
                payment.getIntentId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getTransactionRef(),
                payment.getStatus()
        );
    }
}
