package com.smart.parking.auth;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Long accessTokenExpiresInMs;
    private Long refreshTokenExpiresInMs;
}