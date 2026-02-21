package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerifyDto {

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile must be in E.164 or digits format")
    private String mobile;

    @NotBlank
    @Pattern(regexp = "^\\d{4,8}$", message = "OTP must be numeric")
    private String otp;

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
