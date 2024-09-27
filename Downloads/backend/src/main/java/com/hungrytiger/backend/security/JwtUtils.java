package com.hungrytiger.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // JWT secret key and expiration time are typically stored in application properties
    @Value("${hungrytiger.app.jwtSecret}")
    private String jwtSecret;

    @Value("${hungrytiger.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Generate JWT token
    public String generateJwtToken(Authentication authentication) {
        String email = authentication.getName();

        // Decode the secret key
        Key key = getSigningKey(jwtSecret);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Get email (username) from JWT token
    public String getEmailFromJwtToken(String token) {
        Key key = getSigningKey(jwtSecret);

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate JWT token
    public boolean validateJwtToken(String token) {
        try {
            Key key = getSigningKey(jwtSecret);

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid JWT token
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }

    // Helper method to get the signing key
    private Key getSigningKey(String jwtSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
