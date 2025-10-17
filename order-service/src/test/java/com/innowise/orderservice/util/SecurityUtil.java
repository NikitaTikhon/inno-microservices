package com.innowise.orderservice.util;

import com.innowise.orderservice.model.AuthUser;
import com.innowise.orderservice.model.RoleEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static void setupAuthentication(Long userId, String email, List<RoleEnum> roles, String jwtToken) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(RoleEnum::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        AuthUser authUser = AuthUser.builder()
                .id(userId)
                .email(email)
                .authorities(authorities)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authUser, jwtToken, authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    public static void setupAuthentication(Long userId, String email) {
        setupAuthentication(userId, email, List.of(RoleEnum.ROLE_USER), "test-jwt-token");
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

}

