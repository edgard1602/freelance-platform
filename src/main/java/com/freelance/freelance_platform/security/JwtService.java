package com.freelance.freelance_platform.security;

import com.freelance.freelance_platform.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    // Générer un access token
    public String generateAccessToken(UUID userId, String email, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuer(appProperties.jwt().issuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + appProperties.jwt().accessTokenExpiration() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    // Générer un refresh token
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(appProperties.jwt().issuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + appProperties.jwt().refreshTokenExpiration() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    // Extraire l'userId du token
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    // Extraire le rôle du token
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Valider le token
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Vérifier si le token est expiré
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = appProperties.jwt().secret()
                .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
