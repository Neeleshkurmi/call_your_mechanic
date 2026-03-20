package com.nilesh.cym.repository;

import com.nilesh.cym.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByBooking_Id(Long bookingId);

    Optional<ReviewEntity> findByBooking_Id(Long bookingId);

    List<ReviewEntity> findByMechanic_IdOrderByCreatedAtDesc(Long mechanicId);

    @Query("""
            select coalesce(avg(r.rating), 0)
            from ReviewEntity r
            where r.mechanic.id = :mechanicId
            """)
    Double findAverageRatingByMechanicId(@Param("mechanicId") Long mechanicId);
}
