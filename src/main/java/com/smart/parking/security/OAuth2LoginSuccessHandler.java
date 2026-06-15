package com.smart.parking.security;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * Bridges Spring Security's server-side OAuth2 (Google) login into the app's
 * stateless JWT model. On a successful Google login it finds or creates the user,
 * issues the same access/refresh tokens used by the normal login flow, and redirects
 * the browser back to the frontend callback page with those tokens.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired(required = false)
    private RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${jwt.expiration:900000}")
    private long accessTokenExpirationMs;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(buildErrorRedirect("missing_email"));
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, name));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = "";
        Long refreshTokenExpiresInMs = null;

        if (refreshTokenService != null) {
            RefreshTokenService.RefreshTokenPair pair = refreshTokenService.issueTokens(user, accessToken);
            accessToken = pair.accessToken();
            refreshToken = pair.refreshToken();
            refreshTokenExpiresInMs = pair.refreshTokenExpiresInMs();
        }

        String target = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("expiresIn", accessTokenExpirationMs)
                .queryParam("refreshExpiresIn", refreshTokenExpiresInMs == null ? "" : refreshTokenExpiresInMs)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(target);
    }

    private User createGoogleUser(String email, String name) {
        User user = User.builder()
                .fullName(name != null && !name.isBlank() ? name : email)
                .email(email)
                .phone(null)
                // Random, never-usable password so the NOT NULL column is satisfied
                // while password login remains impossible for Google accounts.
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.DRIVER)
                .authProvider("GOOGLE")
                .build();
        return userRepository.save(user);
    }

    private String buildErrorRedirect(String error) {
        return UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("error", error)
                .build()
                .encode()
                .toUriString();
    }
}
