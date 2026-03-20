package com.nilesh.cym.catalog.service;

import com.nilesh.cym.catalog.dto.ServiceResponseDto;
import com.nilesh.cym.catalog.dto.ServiceEstimateResponseDto;
import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.enums.VehicleType;
import com.nilesh.cym.repository.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceResponseDto> findServices(VehicleType vehicleType) {
        log.debug("service_list_start vehicleType={}", vehicleType);
        List<ServiceEntity> services = vehicleType == null
                ? serviceRepository.findAll()
                : serviceRepository.findByVehicleType(vehicleType);

        List<ServiceResponseDto> responses = services.stream().map(this::toResponse).toList();
        log.info("service_list_success vehicleType={} serviceCount={}", vehicleType, responses.size());
        return responses;
    }

    public ServiceResponseDto findServiceById(Long serviceId) {
        log.debug("service_fetch_start serviceId={}", serviceId);
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        ServiceResponseDto response = toResponse(service);
        log.info("service_fetch_success serviceId={} vehicleType={}", response.id(), response.vehicleType());
        return response;
    }

    public ServiceEstimateResponseDto estimateByServiceId(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        double amount = switch (service.getVehicleType()) {
            case BIKE -> 299D;
            case CAR -> 499D;
            case TRUCK -> 899D;
            case ALL -> 399D;
        };
        int duration = switch (service.getVehicleType()) {
            case BIKE -> 25;
            case CAR -> 40;
            case TRUCK -> 60;
            case ALL -> 30;
        };
        return new ServiceEstimateResponseDto(service.getId(), service.getName(), amount, duration, "Basic service estimate generated successfully");
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
