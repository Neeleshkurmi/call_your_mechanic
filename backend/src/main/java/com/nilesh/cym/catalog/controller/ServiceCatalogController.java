package com.nilesh.cym.catalog.controller;

import com.nilesh.cym.catalog.dto.ServiceResponseDto;
import com.nilesh.cym.catalog.service.ServiceCatalogService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiSchemas;
import com.nilesh.cym.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Public service catalog lookup endpoints.")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    @Operation(summary = "List services", description = "Returns service catalog entries, optionally filtered by vehicle type.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Services fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ServiceListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Vehicle type filter is invalid", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ServiceResponseDto>>> listServices(
            @Parameter(description = "Optional vehicle type used to filter services.", example = "BIKE")
            @RequestParam(required = false) VehicleType vehicleType
    ) {
        log.info("endpoint_request name=listServices vehicleType={}", vehicleType);
        List<ServiceResponseDto> responses = serviceCatalogService.findServices(vehicleType);
        log.info("endpoint_success name=listServices serviceCount={}", responses.size());
        return ResponseEntity.ok(ApiResponse.success("Services fetched successfully", responses));
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Get service by id", description = "Returns a service catalog item by its identifier.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ServiceApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<ServiceResponseDto>> getServiceById(@PathVariable Long serviceId) {
        log.info("endpoint_request name=getServiceById serviceId={}", serviceId);
        ServiceResponseDto response = serviceCatalogService.findServiceById(serviceId);
        log.info("endpoint_success name=getServiceById serviceId={} vehicleType={}", response.id(), response.vehicleType());
        return ResponseEntity.ok(ApiResponse.success("Service fetched successfully", response));
    }
}
