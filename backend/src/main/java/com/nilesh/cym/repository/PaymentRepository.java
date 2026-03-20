package com.nilesh.cym.repository;

import com.nilesh.cym.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findTopByBooking_IdOrderByCreatedAtDesc(Long bookingId);

    Optional<PaymentEntity> findByReference(String reference);

    List<PaymentEntity> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);
}
