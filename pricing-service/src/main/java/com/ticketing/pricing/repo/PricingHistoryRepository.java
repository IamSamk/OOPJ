package com.ticketing.pricing.repo;

import com.ticketing.pricing.domain.PricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingHistoryRepository extends JpaRepository<PricingHistory, UUID> {

    List<PricingHistory> findTop20ByEventIdOrderByCreatedAtDesc(UUID eventId);
}
