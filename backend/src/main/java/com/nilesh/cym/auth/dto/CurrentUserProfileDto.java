package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CurrentUserProfile", description = "Current user profile details returned after a successful authenticated update.")
public record CurrentUserProfileDto(
        @Schema(description = "Unique identifier of the current user.", example = "42")
        Long userId,
        @Schema(description = "Display name of the current user.", example = "Nilesh Patil")
        String name,
        @Schema(description = "Verified mobile number of the current user.", example = "+919876543210")
        String mobile,
        @Schema(description = "Current role assigned to the user.", example = "MECHANIC")
        UserRole role,
        @Schema(description = "Whether the user has already finished the one-time post-login profile setup.", example = "true")
        boolean profileCompleted
) {
}
