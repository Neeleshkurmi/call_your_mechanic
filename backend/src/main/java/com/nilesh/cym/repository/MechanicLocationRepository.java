package com.nilesh.cym.repository;

import com.nilesh.cym.entity.MechanicLocationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MechanicLocationRepository extends JpaRepository<MechanicLocationEntity, Long> {
    Optional<MechanicLocationEntity> findTopByMechanic_IdOrderByRecordedAtDesc(Long mechanicId);

    List<MechanicLocationEntity> findByMechanic_IdAndRecordedAtGreaterThanEqualOrderByRecordedAtDesc(Long mechanicId, Instant since, Pageable pageable);
}
