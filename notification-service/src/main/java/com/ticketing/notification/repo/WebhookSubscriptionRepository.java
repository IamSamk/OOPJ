package com.ticketing.notification.repo;

import com.ticketing.notification.domain.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {

    List<WebhookSubscription> findByEventTypeIgnoreCaseAndActiveTrue(String eventType);
}
