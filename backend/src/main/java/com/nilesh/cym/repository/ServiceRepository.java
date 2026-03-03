package com.nilesh.cym.repository;

import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByVehicleType(VehicleType vehicleType);
}
