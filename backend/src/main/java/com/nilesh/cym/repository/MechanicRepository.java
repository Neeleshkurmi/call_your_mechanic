package com.nilesh.cym.repository;

import com.nilesh.cym.entity.MechanicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MechanicRepository extends JpaRepository<MechanicEntity, Long> {
    Optional<MechanicEntity> findByUser_Id(Long userId);
}
