package com.businesspilot.common.security;

import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && jwtService.validateToken(token)) {
                UUID userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);
                UUID orgId = jwtService.extractOrgId(token);
                String role = jwtService.extractRole(token);
                boolean superAdmin = jwtService.extractSuperAdmin(token);

                UserPrincipal principal = UserPrincipal.builder()
                    .userId(userId)
                    .email(email)
                    .orgId(orgId)
                    .role(role)
                    .onboardingRequired(false)
                    .superAdmin(superAdmin)
                    .build();

                var auth = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                if (orgId != null) {
                    TenantContext.setCurrentTenant(orgId.toString());
                }
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
