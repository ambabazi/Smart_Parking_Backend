package com.smart.parking.security;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class AppUserDetailsTest {

    private User testUser;
    private AppUserDetails appUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("hashed_password")
                .role(Role.DRIVER)
                .plateNumber("ABC123")
                .build();

        appUserDetails = new AppUserDetails(testUser);
    }

    @Test
    void constructor_ShouldStoreUserId() {
        // Assert
        assertEquals(1L, appUserDetails.getId());
    }

    @Test
    void constructor_ShouldStoreEmail() {
        // Assert
        assertEquals("test@example.com", appUserDetails.getEmail());
    }

    @Test
    void constructor_ShouldStorePassword() {
        // Assert
        assertEquals("hashed_password", appUserDetails.getPassword());
    }

    @Test
    void constructor_ShouldCreateAuthoritiesFromRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = appUserDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("DRIVER")));
    }

    @Test
    void getId_ShouldReturnUserId() {
        // Assert
        assertEquals(1L, appUserDetails.getId());
    }

    @Test
    void getEmail_ShouldReturnUserEmail() {
        // Assert
        assertEquals("test@example.com", appUserDetails.getEmail());
    }

    @Test
    void getPassword_ShouldReturnHashedPassword() {
        // Assert
        assertEquals("hashed_password", appUserDetails.getPassword());
    }

    @Test
    void getUsername_ShouldReturnEmail() {
        // Assert
        assertEquals("test@example.com", appUserDetails.getUsername());
    }

    @Test
    void getAuthorities_ShouldReturnRoleAsAuthority() {
        // Act
        Collection<? extends GrantedAuthority> authorities = appUserDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.stream().findFirst().orElse(null);
        assertNotNull(authority);
        assertEquals("DRIVER", authority.getAuthority());
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        // Assert
        assertTrue(appUserDetails.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        // Assert
        assertTrue(appUserDetails.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Assert
        assertTrue(appUserDetails.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        // Assert
        assertTrue(appUserDetails.isEnabled());
    }

    @Test
    void constructor_ShouldHandleHostRole() {
        // Arrange
        User hostUser = User.builder()
                .id(2L)
                .name("Host User")
                .email("host@example.com")
                .password("hashed")
                .role(Role.HOST)
                .build();

        // Act
        AppUserDetails hostDetails = new AppUserDetails(hostUser);

        // Assert
        assertEquals(2L, hostDetails.getId());
        assertEquals("host@example.com", hostDetails.getEmail());
        assertEquals(Role.HOST.name(), hostDetails.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }

    @Test
    void constructor_ShouldHandleAdminRole() {
        // Arrange
        User adminUser = User.builder()
                .id(3L)
                .name("Admin User")
                .email("admin@example.com")
                .password("hashed")
                .role(Role.ADMIN)
                .build();

        // Act
        AppUserDetails adminDetails = new AppUserDetails(adminUser);

        // Assert
        assertEquals(3L, adminDetails.getId());
        assertEquals(Role.ADMIN.name(), adminDetails.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }

    @Test
    void userDetailsContract_ShouldBeImplementedCorrectly() {
        // Arrange - AppUserDetails should be castable to UserDetails
        org.springframework.security.core.userdetails.UserDetails userDetails = appUserDetails;

        // Assert - All UserDetails methods should work
        assertNotNull(userDetails.getPassword());
        assertNotNull(userDetails.getUsername());
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void getId_ShouldBeAccessibleViawithLombok() {
        // Assert - Verify Lombok @Getter works
        assertEquals(1L, appUserDetails.getId());
    }

    @Test
    void constructor_ShouldPreserveAllUserData() {
        // Arrange
        User complexUser = User.builder()
                .id(100L)
                .name("Complex User")
                .email("complex@example.com")
                .password("very_hashed_password")
                .role(Role.DRIVER)
                .plateNumber("XYZ789")
                .build();

        // Act
        AppUserDetails details = new AppUserDetails(complexUser);

        // Assert
        assertEquals(100L, details.getId());
        assertEquals("complex@example.com", details.getEmail());
        assertEquals("very_hashed_password", details.getPassword());
        assertEquals("complex@example.com", details.getUsername());
        assertEquals("DRIVER", details.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }
}
