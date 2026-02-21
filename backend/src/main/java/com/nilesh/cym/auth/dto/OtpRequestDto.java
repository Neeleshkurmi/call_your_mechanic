package com.nilesh.cym.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpRequestDto {

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile must be in E.164 or digits format")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
