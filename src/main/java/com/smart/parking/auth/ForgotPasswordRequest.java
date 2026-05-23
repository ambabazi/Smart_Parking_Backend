package com.smart.parking.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String email;
}