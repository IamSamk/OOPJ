package com.ticketing.booking.repo;

import com.ticketing.booking.domain.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByBookingRef(String bookingRef);
}
