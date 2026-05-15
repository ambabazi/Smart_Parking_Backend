package com.smart.parking.auth;
import com.smart.parking.common.ApiResponse;
import com.smart.parking.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Agnes");
        request.setEmail("agnes@test.com");
        request.setPassword("Pass@1234");
        request.setPhone("+250788123456");
        request.setRole(Role.DRIVER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_pass");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock_jwt_token");
        User savedUser = User.builder()
                .id(1L)
                .fullName("Agnes")
                .email("agnes@test.com")
                .phone("+250788123456")
                .password("hashed_pass")
                .role(Role.DRIVER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.register(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(201, responseEntity.getStatusCodeValue());
        ApiResponse<AuthResponse> body = responseEntity.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Registration successful", body.getMessage());
        AuthResponse response = body.getData();
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("Agnes", response.getFullName());
        assertEquals("agnes@test.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Agnes");
        request.setEmail("agnes@test.com");
        request.setPassword("Pass@1234");
        request.setPhone("+250788123456");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.register(request);

        assertEquals(400, responseEntity.getStatusCodeValue());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("Email already registered", responseEntity.getBody().getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldSetDefaultRoleAsDriver_WhenRoleIsNull() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John");
        request.setEmail("john@test.com");
        request.setPassword("Pass@1234");
        request.setPhone("+250788654321");
        request.setRole(null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_pass");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock_jwt_token");
        User savedUser = User.builder()
                .id(2L)
                .fullName("John")
                .email("john@test.com")
                .phone("+250788654321")
                .password("hashed_pass")
                .role(Role.DRIVER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.register(request);

        // Assert
        ApiResponse<AuthResponse> body = responseEntity.getBody();
        assertTrue(body.isSuccess());
        AuthResponse response = body.getData();
        assertEquals(Role.DRIVER.name(), response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateAndReturnToken_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("agnes@test.com");
        request.setPassword("pass123");

        User user = User.builder()
                .id(1L)
                .fullName("Agnes")
                .email("agnes@test.com")
                .phone("+250788123456")
                .password("hashed_pass")
                .role(Role.DRIVER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("login_jwt_token");

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.login(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        ApiResponse<AuthResponse> body = responseEntity.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Login successful", body.getMessage());
        AuthResponse response = body.getData();
        assertEquals("login_jwt_token", response.getToken());
        assertEquals("agnes@test.com", response.getEmail());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@test.com");
        request.setPassword("pass123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.login(request);

        assertEquals(401, responseEntity.getStatusCodeValue());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("Invalid email or password", responseEntity.getBody().getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("agnes@test.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authService.login(request);

        assertEquals(401, responseEntity.getStatusCodeValue());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("Invalid email or password", responseEntity.getBody().getMessage());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}
