package com.ticketing.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.common.kafka.TopicNames;
import com.ticketing.notification.api.WebhookRegistrationResponse;
import com.ticketing.notification.domain.WebhookDelivery;
import com.ticketing.notification.domain.WebhookSubscription;
import com.ticketing.notification.repo.WebhookDeliveryRepository;
import com.ticketing.notification.repo.WebhookSubscriptionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class WebhookDispatcherService {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public WebhookDispatcherService(WebhookSubscriptionRepository subscriptionRepository,
                                    WebhookDeliveryRepository deliveryRepository,
                                    KafkaTemplate<String, Object> kafkaTemplate,
                                    ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<WebhookRegistrationResponse> register(com.ticketing.notification.api.WebhookRegistrationRequest request) {
        return Mono.fromCallable(() -> {
            WebhookSubscription subscription = new WebhookSubscription();
            subscription.setCallbackUrl(request.callbackUrl());
            subscription.setEventType(request.eventType());
            subscription.setSecret(request.secret());
            WebhookSubscription saved = subscriptionRepository.save(subscription);
            return new WebhookRegistrationResponse(saved.getId(), saved.getCallbackUrl(), saved.getEventType(), saved.isActive());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public void dispatch(String eventType, Object payload) {
        List<WebhookSubscription> subscriptions = subscriptionRepository.findByEventTypeIgnoreCaseAndActiveTrue(eventType);
        String json = serialize(payload);
        subscriptions.forEach(subscription -> deliverWithRetry(subscription, eventType, json, 0));
    }

    private void deliverWithRetry(WebhookSubscription subscription, String eventType, String jsonPayload, int attempt) {
        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setSubscriptionId(subscription.getId());
        delivery.setEventType(eventType);
        delivery.setPayload(jsonPayload);
        delivery.setAttempts(attempt + 1);
        delivery.setLastAttemptAt(Instant.now());
        delivery.setStatus("PENDING");
        deliveryRepository.save(delivery);

        WebClient client = WebClient.builder().build();
        client.post()
                .uri(subscription.getCallbackUrl())
                .header("X-Webhook-Event", eventType)
                .bodyValue(jsonPayload)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(2)))
                .doOnSuccess(response -> {
                    delivery.setStatus("SUCCESS");
                    deliveryRepository.save(delivery);
                })
                .doOnError(error -> {
                    delivery.setStatus("FAILED");
                    deliveryRepository.save(delivery);
                    kafkaTemplate.send(TopicNames.WEBHOOK_DLQ, eventType, jsonPayload);
                })
                .subscribe();
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize webhook payload", exception);
        }
    }
}
