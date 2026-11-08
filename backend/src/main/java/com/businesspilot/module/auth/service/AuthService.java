package com.businesspilot.module.auth.service;

import com.businesspilot.common.exception.BusinessException;
import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.security.UserPrincipal;
import com.businesspilot.module.auth.domain.*;
import com.businesspilot.module.auth.dto.*;
import com.businesspilot.module.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email().toLowerCase())) {
            throw new BusinessException("Un compte existe déjà avec cet email");
        }

        Organization org = Organization.builder()
            .name(req.organizationName())
            .slug(generateSlug(req.organizationName()))
            .email(req.email().toLowerCase())
            .countryCode("CA")
            .currency("CAD")
            .locale("fr-CA")
            .timezone("America/Toronto")
            .active(true)
            .build();
        org = organizationRepository.save(org);

        User user = User.builder()
            .email(req.email().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.password()))
            .firstName(req.firstName())
            .lastName(req.lastName())
            .provider(AuthProvider.EMAIL)
            .emailVerified(false)
            .active(true)
            .build();
        user = userRepository.save(user);

        UserOrganizationRole role = UserOrganizationRole.builder()
            .userId(user.getId())
            .organizationId(org.getId())
            .role(UserRole.OWNER)
            .build();
        roleRepository.save(role);

        return issueTokens(user, org.getId(), UserRole.OWNER, false);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect"));

        if (!user.isActive()) {
            throw new BusinessException("Compte désactivé");
        }
        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BusinessException("Ce compte utilise la connexion Google");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("Email ou mot de passe incorrect");
        }

        UserOrganizationRole role = roleRepository.findFirstByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Organisation introuvable"));

        return issueTokens(user, role.getOrganizationId(), role.getRole(), false);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        String hash = hashToken(req.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new BusinessException("Token de rafraîchissement invalide"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException("Token de rafraîchissement expiré");
        }

        User user = userRepository.findById(stored.getUserId())
            .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        UserOrganizationRole role = roleRepository.findFirstByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Organisation introuvable"));

        // Rotation du refresh token
        refreshTokenRepository.delete(stored);

        return issueTokens(user, role.getOrganizationId(), role.getRole(), false);
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = hashToken(refreshToken);
        refreshTokenRepository.deleteByTokenHash(hash);
    }

    public AuthResponse issueTokens(User user, UUID orgId, UserRole role, boolean onboardingRequired) {
        UserPrincipal principal = UserPrincipal.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .orgId(orgId)
            .role(role != null ? role.name() : null)
            .onboardingRequired(onboardingRequired)
            .superAdmin(user.isSuperAdmin())
            .build();

        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefresh = jwtService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(hashToken(rawRefresh))
            .expiresAt(Instant.now().plusMillis(refreshExpiryMs))
            .revoked(false)
            .createdAt(Instant.now())
            .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
            accessToken,
            rawRefresh,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            orgId,
            role != null ? role.name() : null,
            onboardingRequired,
            user.isSuperAdmin(),
            user.getPreferredLocale() != null ? user.getPreferredLocale() : "fr"
        );
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        String candidate = base + "-" + suffix;
        // Ensure uniqueness (very low probability of collision with suffix)
        int attempt = 0;
        while (organizationRepository.existsBySlug(candidate) && attempt < 5) {
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 6);
            attempt++;
        }
        return candidate;
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
