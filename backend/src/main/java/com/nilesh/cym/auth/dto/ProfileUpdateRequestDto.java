package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ProfileUpdateRequest", description = "Authenticated request payload used to update the current user's profile.")
public class ProfileUpdateRequestDto {

    @NotBlank
    @Size(max = 120)
    @Schema(description = "Display name for the current user.", example = "Nilesh Patil", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @Schema(description = "Role to assign to the current user.", example = "MECHANIC", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
