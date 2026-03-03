package com.nilesh.cym.repository;

import com.nilesh.cym.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUser_IdOrderByBookingTimeDesc(Long userId);

    List<BookingEntity> findByMechanic_IdOrderByBookingTimeDesc(Long mechanicId);
}
