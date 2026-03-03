package com.nilesh.cym.catalog.dto;

import com.nilesh.cym.entity.enums.VehicleType;

public record ServiceResponseDto(
        Long id,
        String name,
        String description,
        VehicleType vehicleType
) {
}
