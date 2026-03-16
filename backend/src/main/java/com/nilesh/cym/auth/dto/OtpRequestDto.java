package com.nilesh.cym.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OtpRequest", description = "Request payload used to send an OTP to a mobile number.")
public class OtpRequestDto {

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile must be in E.164 or digits format")
    @Schema(description = "User mobile number in E.164 or digits format.", example = "+919876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
