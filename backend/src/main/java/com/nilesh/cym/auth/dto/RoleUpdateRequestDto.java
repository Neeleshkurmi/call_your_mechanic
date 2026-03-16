package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(name = "RoleUpdateRequest", description = "Request payload used to update the role assigned to a mobile number.")
public class RoleUpdateRequestDto {

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile must be in E.164 or digits format")
    @Schema(description = "User mobile number in E.164 or digits format.", example = "+919876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;

    @NotNull
    @Schema(description = "Role that should be assigned to the user.", example = "MECHANIC", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
