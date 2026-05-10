package com.smart.parking.security;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "app.jwt.secret=test-secret-key-that-is-long-enough-for-testing-purposes")
class JwtServiceTest {

    private JwtService jwtService;

    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Use reflection to set the secret key
        ReflectionTestUtils.setField(jwtService, "secretKey", "test-secret-key-that-is-long-enough-for-testing-purposes");

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.DRIVER)
                .plateNumber("ABC123")
                .build();

        testUserDetails = new AppUserDetails(testUser);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
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
    void generateToken_ShouldIncludeRoleInClaims() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("DRIVER", role);
    }

    @Test
    void generateToken_ShouldIncludeUserIdInClaims() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        assertEquals(1L, userId);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForTokenWithWrongUser() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        UserDetails differentUser = new AppUserDetails(User.builder()
                .id(2L)
                .email("different@example.com")
                .password("hashed")
                .role(Role.DRIVER)
                .build());

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void extractClaim_ShouldReturnClaimValue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Assert
        assertEquals("DRIVER", role);
    }
}
