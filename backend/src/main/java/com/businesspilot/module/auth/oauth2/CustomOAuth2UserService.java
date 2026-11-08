package com.businesspilot.module.auth.oauth2;

import com.businesspilot.module.auth.domain.AuthProvider;
import com.businesspilot.module.auth.domain.User;
import com.businesspilot.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email non fourni par Google");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseGet(() -> {
                User newUser = User.builder()
                    .email(email.toLowerCase())
                    .firstName(firstName)
                    .lastName(lastName)
                    .provider(AuthProvider.GOOGLE)
                    .emailVerified(true)
                    .active(true)
                    .build();
                return userRepository.save(newUser);
            });

        // Update profile if changed on Google side
        if (user.getProvider() == AuthProvider.GOOGLE) {
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
            }
            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
            }
            userRepository.save(user);
        }

        return new OAuth2UserAdapter(oAuth2User, user.getId());
    }
}
