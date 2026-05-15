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

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phone("+250788123456")
                .password("hashed_password")
                .role(Role.DRIVER)
                .build();
    }

    @Test
    void constructor_ShouldStoreUserId() {
        // Assert
        assertEquals(1L, testUser.getId());
    }

    @Test
    void constructor_ShouldStoreEmail() {
        // Assert
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void constructor_ShouldStorePassword() {
        // Assert
        assertEquals("hashed_password", testUser.getPassword());
    }

    @Test
    void constructor_ShouldCreateAuthoritiesFromRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = testUser.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("DRIVER")));
    }

    @Test
    void getId_ShouldReturnUserId() {
        // Assert
        assertEquals(1L, testUser.getId());
    }

    @Test
    void getEmail_ShouldReturnUserEmail() {
        // Assert
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void getPassword_ShouldReturnHashedPassword() {
        // Assert
        assertEquals("hashed_password", testUser.getPassword());
    }

    @Test
    void getUsername_ShouldReturnEmail() {
        // Assert
        assertEquals("test@example.com", testUser.getUsername());
    }

    @Test
    void getAuthorities_ShouldReturnRoleAsAuthority() {
        // Act
        Collection<? extends GrantedAuthority> authorities = testUser.getAuthorities();

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
        assertTrue(testUser.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        // Assert
        assertTrue(testUser.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Assert
        assertTrue(testUser.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        // Assert
        assertTrue(testUser.isEnabled());
    }

    @Test
    void constructor_ShouldHandleHostRole() {
        // Arrange
        User hostUser = User.builder()
                .id(2L)
                .fullName("Host User")
                .email("host@example.com")
                .phone("+250788123457")
                .password("hashed")
                .role(Role.HOST)
                .build();

        // Act & Assert
        assertEquals(2L, hostUser.getId());
        assertEquals("host@example.com", hostUser.getEmail());
        assertEquals("HOST", hostUser.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }

    @Test
    void constructor_ShouldHandleAdminRole() {
        // Arrange
        User adminUser = User.builder()
                .id(3L)
                .fullName("Admin User")
                .email("admin@example.com")
                .phone("+250788123458")
                .password("hashed")
                .role(Role.ADMIN)
                .build();

        // Act & Assert
        assertEquals(3L, adminUser.getId());
        assertEquals("ADMIN", adminUser.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }

    @Test
    void userDetailsContract_ShouldBeImplementedCorrectly() {
        // Arrange - User should implement UserDetails
        org.springframework.security.core.userdetails.UserDetails userDetails = testUser;

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
    void getId_ShouldBeAccessibleViaLombok() {
        // Assert - Verify Lombok @Getter works
        assertEquals(1L, testUser.getId());
    }

    @Test
    void constructor_ShouldPreserveAllUserData() {
        // Arrange
        User complexUser = User.builder()
                .id(100L)
                .fullName("Complex User")
                .email("complex@example.com")
                .phone("+250788999999")
                .password("very_hashed_password")
                .role(Role.DRIVER)
                .build();

        // Assert
        assertEquals(100L, complexUser.getId());
        assertEquals("complex@example.com", complexUser.getEmail());
        assertEquals("very_hashed_password", complexUser.getPassword());
        assertEquals("complex@example.com", complexUser.getUsername());
        assertEquals("DRIVER", complexUser.getAuthorities().stream()
                .findFirst()
                .orElse(null)
                .getAuthority());
    }
}
