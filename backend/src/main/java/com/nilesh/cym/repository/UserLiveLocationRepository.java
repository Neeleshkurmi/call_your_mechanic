package com.nilesh.cym.repository;

import com.nilesh.cym.entity.UserLiveLocationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserLiveLocationRepository extends JpaRepository<UserLiveLocationEntity, Long> {
    Optional<UserLiveLocationEntity> findTopByUser_IdOrderByRecordedAtDesc(Long userId);

    List<UserLiveLocationEntity> findByUser_IdAndRecordedAtGreaterThanEqualOrderByRecordedAtDesc(Long userId, Instant since, Pageable pageable);
}
