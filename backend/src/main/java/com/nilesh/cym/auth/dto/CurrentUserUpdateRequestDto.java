package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CurrentUserUpdateRequest", description = "Payload used to update the current user's profile without refreshing tokens.")
public record CurrentUserUpdateRequestDto(
        @NotBlank @Size(max = 120) @Schema(example = "Nilesh Patil") String name,
        @NotNull @Schema(example = "USER") UserRole role
) {
}
