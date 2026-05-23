package com.smart.parking.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminStaffResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String staffRole;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}