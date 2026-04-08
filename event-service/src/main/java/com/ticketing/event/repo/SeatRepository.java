package com.ticketing.event.repo;

import com.ticketing.event.domain.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {

    List<SeatEntity> findByEvent_Id(UUID eventId);

    List<SeatEntity> findByEvent_IdAndIdIn(UUID eventId, List<UUID> ids);
}
