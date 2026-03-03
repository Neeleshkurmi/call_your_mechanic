package com.nilesh.cym.catalog.controller;

import com.nilesh.cym.catalog.dto.ServiceResponseDto;
import com.nilesh.cym.catalog.service.ServiceCatalogService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.entity.enums.VehicleType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponseDto>>> listServices(
            @RequestParam(required = false) VehicleType vehicleType
    ) {
        return ResponseEntity.ok(ApiResponse.success("Services fetched successfully", serviceCatalogService.findServices(vehicleType)));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> getServiceById(@PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success("Service fetched successfully", serviceCatalogService.findServiceById(serviceId)));
    }
}
