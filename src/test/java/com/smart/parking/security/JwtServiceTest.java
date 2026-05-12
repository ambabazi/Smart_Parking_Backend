package com.smart.parking.security;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Use reflection to set the secret and expiration
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-that-is-long-enough-for-testing-purposes");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);

        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phone("+250788123456")
                .password("hashedPassword")
                .role(Role.DRIVER)
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateToken_ShouldIncludeUserEmail() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void extractUsername_ShouldReturnEmailFromToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extracted = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extracted);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForTokenWithWrongUser() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        User differentUser = User.builder()
                .id(2L)
                .fullName("Different User")
                .email("different@example.com")
                .phone("+250788654321")
                .password("hashed")
                .role(Role.DRIVER)
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForExpiredToken() {
        // Arrange - Create token with very short expiration
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(testUser);
        
        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForMalformedToken() {
        // Arrange
        String malformedToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken, testUser);

        // Assert
        assertFalse(isValid);
    }
}
