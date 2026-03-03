package com.nilesh.cym.catalog.service;

import com.nilesh.cym.catalog.dto.ServiceResponseDto;
import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.enums.VehicleType;
import com.nilesh.cym.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceResponseDto> findServices(VehicleType vehicleType) {
        List<ServiceEntity> services = vehicleType == null
                ? serviceRepository.findAll()
                : serviceRepository.findByVehicleType(vehicleType);

        return services.stream().map(this::toResponse).toList();
    }

    public ServiceResponseDto findServiceById(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        return toResponse(service);
    }

    private ServiceResponseDto toResponse(ServiceEntity entity) {
        return new ServiceResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVehicleType()
        );
    }
}
