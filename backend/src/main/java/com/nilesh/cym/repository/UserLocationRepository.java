package com.nilesh.cym.repository;

import com.nilesh.cym.entity.UserLocationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocationEntity, Long> {
    Optional<UserLocationEntity> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    List<UserLocationEntity> findByUser_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Long userId, Instant since, Pageable pageable);
}
