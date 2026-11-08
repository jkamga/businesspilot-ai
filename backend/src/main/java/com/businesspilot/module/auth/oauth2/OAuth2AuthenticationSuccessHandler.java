package com.businesspilot.module.auth.oauth2;

import com.businesspilot.module.auth.domain.User;
import com.businesspilot.module.auth.domain.UserRole;
import com.businesspilot.module.auth.repository.UserOrganizationRoleRepository;
import com.businesspilot.module.auth.repository.UserRepository;
import com.businesspilot.module.auth.service.AuthService;
import com.businesspilot.module.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserOrganizationRoleRepository roleRepository;
    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        OAuth2UserAdapter oAuth2User = (OAuth2UserAdapter) authentication.getPrincipal();

        User user = userRepository.findById(oAuth2User.userId())
            .orElseThrow(() -> new IllegalStateException("User not found after OAuth2"));

        var roleOpt = roleRepository.findFirstByUserId(user.getId());
        boolean onboardingRequired = roleOpt.isEmpty();

        AuthResponse tokens;
        if (roleOpt.isPresent()) {
            var role = roleOpt.get();
            tokens = authService.issueTokens(user, role.getOrganizationId(), role.getRole(), false);
        } else {
            tokens = authService.issueTokens(user, null, UserRole.OWNER, true);
        }

        String redirectUrl = UriComponentsBuilder
            .fromUriString(frontendUrl + "/auth/oauth2/success")
            .queryParam("token", tokens.accessToken())
            .queryParam("refresh", tokens.refreshToken())
            .queryParam("onboarding", onboardingRequired)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
