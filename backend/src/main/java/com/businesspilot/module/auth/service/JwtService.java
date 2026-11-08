package com.businesspilot.module.auth.service;

import com.businesspilot.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiryMs;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiry-ms}") long expiryMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiryMs = expiryMs;
    }

    public String generateAccessToken(UserPrincipal principal) {
        return Jwts.builder()
            .subject(principal.getUsername())
            .claim("userId", principal.getUserId().toString())
            .claim("orgId", principal.getOrgId() != null ? principal.getOrgId().toString() : null)
            .claim("role", principal.getRole())
            .claim("onboardingRequired", principal.isOnboardingRequired())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(secretKey)
            .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String id = extractAllClaims(token).get("userId", String.class);
        return id != null ? UUID.fromString(id) : null;
    }

    public UUID extractOrgId(String token) {
        String id = extractAllClaims(token).get("orgId", String.class);
        return id != null ? UUID.fromString(id) : null;
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
