package com.businesspilot.common.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final UUID orgId;
    private final String role;
    private final boolean onboardingRequired;
    private final boolean superAdmin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        if (role != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        if (superAdmin) authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
