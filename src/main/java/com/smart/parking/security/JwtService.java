package com.smart.parking.security;

import com.smart.parking.auth.User;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public String generateToken(User user) {
        // Minimal placeholder implementation for startup; replace with real JWT generation
        return "token-for-user-" + (user != null ? (user.getId() != null ? user.getId() : user.getEmail()) : "anonymous");
    }
}
