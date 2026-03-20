package com.nilesh.cym.repository;

import com.nilesh.cym.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocationEntity, Long> {
    List<UserLocationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<UserLocationEntity> findByIdAndUser_Id(Long locationId, Long userId);
}
