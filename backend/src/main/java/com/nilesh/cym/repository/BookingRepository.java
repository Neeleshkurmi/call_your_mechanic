package com.nilesh.cym.repository;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUser_IdOrderByBookingTimeDesc(Long userId);

    List<BookingEntity> findByMechanic_IdOrderByBookingTimeDesc(Long mechanicId);

    List<BookingEntity> findByUser_IdAndStatusInOrderByBookingTimeDesc(Long userId, List<BookingStatus> statuses);

    List<BookingEntity> findByMechanic_IdAndStatusInOrderByBookingTimeDesc(Long mechanicId, List<BookingStatus> statuses);

    Optional<BookingEntity> findByIdAndUser_Id(Long bookingId, Long userId);

    Optional<BookingEntity> findByIdAndMechanic_Id(Long bookingId, Long mechanicId);
}
