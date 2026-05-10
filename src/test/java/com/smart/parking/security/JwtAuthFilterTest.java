package com.smart.parking.security;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSkipFilter_WhenAuthHeaderIsAbsent() throws Exception {
        // Arrange
        request.removeHeader("Authorization");

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldSkipFilter_WhenAuthHeaderDoesNotStartWithBearer() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldExtractTokenFromHeader_WhenBearerTokenProvided() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String email = "user@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(anyString())).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(new AppUserDetails(User.builder()
                        .id(1L)
                        .email(email)
                        .password("hashed")
                        .role(Role.DRIVER)
                        .build()));
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        verify(jwtService, times(1)).extractUsername(token);
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String email = "user@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed")
                .role(Role.DRIVER)
                .build();
        AppUserDetails userDetails = new AppUserDetails(user);

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenTokenIsInvalid() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String email = "user@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed")
                .role(Role.DRIVER)
                .build();
        AppUserDetails userDetails = new AppUserDetails(user);

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenUserNotFound() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String email = "nonexistent@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            jwtAuthFilter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void doFilterInternal_ShouldProceedWithFilterChain_WhenFilterCompletes() throws Exception {
        // Arrange
        request.removeHeader("Authorization");

        // Act & Assert - Filter should complete without errors
        assertDoesNotThrow(() -> jwtAuthFilter.doFilter(request, response, filterChain));
    }

    @Test
    void doFilterInternal_ShouldExtractEmailFromToken() throws Exception {
        // Arrange
        String token = "jwt-token";
        String email = "driver@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(email);

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        verify(jwtService, times(1)).extractUsername(token);
    }
}
