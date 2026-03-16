package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "OtpVerifyRequest", description = "Request payload used to verify an OTP and issue JWT tokens.")
public class OtpVerifyDto {

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile must be in E.164 or digits format")
    @Schema(description = "User mobile number in E.164 or digits format.", example = "+919876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;

    @NotBlank
    @Pattern(regexp = "^\\d{4,8}$", message = "OTP must be numeric")
    @Schema(description = "Numeric OTP received by the user.", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String otp;

    @Schema(description = "Role to assign the user at verification time.", example = "USER", defaultValue = "USER")
    private UserRole role = UserRole.USER;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
