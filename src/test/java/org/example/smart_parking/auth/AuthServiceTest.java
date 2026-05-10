package com.smart.parking.auth;

import com.smart.parking.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldSaveUserAndReturnToken_WhenEmailIsUnique() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Agnes", "agnes@test.com", "pass123", Role.DRIVER, "RAB123C");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_pass");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock_jwt_token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("Agnes", response.getUser().getName());
        assertEquals("agnes@test.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Agnes", "agnes@test.com", "pass123", Role.DRIVER, "RAB123C");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldSetDefaultRoleAsDriver_WhenRoleIsNull() {
        // Arrange
        RegisterRequest request = new RegisterRequest("John", "john@test.com", "pass123", null, "ABC456");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_pass");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock_jwt_token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertEquals(Role.DRIVER, response.getUser().getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateAndReturnToken_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("agnes@test.com", "pass123");
        User user = User.builder()
                .id(1L)
                .name("Agnes")
                .email("agnes@test.com")
                .password("hashed_pass")
                .role(Role.DRIVER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("login_jwt_token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("login_jwt_token", response.getToken());
        assertEquals("agnes@test.com", response.getUser().getEmail());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("notfound@test.com", "pass123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // Arrange
        LoginRequest request = new LoginRequest("agnes@test.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> {
            authService.login(request);
        });

        verify(jwtService, never()).generateToken(any(User.class));
    }
}
