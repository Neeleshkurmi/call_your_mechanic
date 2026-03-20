package com.nilesh.cym.repository;

import com.nilesh.cym.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    List<VehicleEntity> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<VehicleEntity> findByIdAndUser_Id(Long vehicleId, Long userId);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long vehicleId);
}
