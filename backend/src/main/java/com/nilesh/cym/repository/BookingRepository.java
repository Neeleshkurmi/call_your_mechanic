package com.nilesh.cym.repository;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUser_IdOrderByBookingTimeDesc(Long userId);

    List<BookingEntity> findByMechanic_IdOrderByBookingTimeDesc(Long mechanicId);

    List<BookingEntity> findByUser_IdAndStatusInOrderByBookingTimeDesc(Long userId, List<BookingStatus> statuses);

    List<BookingEntity> findByMechanic_IdAndStatusInOrderByBookingTimeDesc(Long mechanicId, List<BookingStatus> statuses);

    List<BookingEntity> findByUser_IdAndStatusOrderByBookingTimeDesc(Long userId, BookingStatus status);

    List<BookingEntity> findByMechanic_IdAndStatusOrderByBookingTimeDesc(Long mechanicId, BookingStatus status);

    Optional<BookingEntity> findByIdAndUser_Id(Long bookingId, Long userId);

    Optional<BookingEntity> findByIdAndMechanic_Id(Long bookingId, Long mechanicId);

    @Query("""
            select b
            from BookingEntity b
            join fetch b.user bu
            join fetch b.mechanic bm
            join fetch bm.user mu
            where b.id = :bookingId
            """)
    Optional<BookingEntity> findWithParticipantsById(@Param("bookingId") Long bookingId);
}
