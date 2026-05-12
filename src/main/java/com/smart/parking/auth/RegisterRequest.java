package com.smart.parking.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Valid email required")
    @NotBlank
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+250[0-9]{9}$",
             message = "Phone must be in format +250XXXXXXXXX")
    private String phone;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // DRIVER by default; pass HOST to register as host
    private Role role = Role.DRIVER;
}