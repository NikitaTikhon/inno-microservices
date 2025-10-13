package com.innowise.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Represents an authenticated user in the Spring Security context.
 * This class implements {@link UserDetails} to integrate with Spring Security
 * and holds user information extracted from JWT tokens.
 * <p>
 * Used primarily for JWT-based authentication where user details are derived
 * from token claims rather than database lookups.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements UserDetails {

    /**
     * The unique identifier of the user.
     */
    private Long id;

    /**
     * The email address of the user, used as the username.
     */
    private String email;

    /**
     * The list of granted authorities (roles) for the user.
     */
    private List<? extends GrantedAuthority> authorities;

    /**
     * Returns the authorities granted to the user.
     *
     * @return A collection of {@link GrantedAuthority} representing the user's roles.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     * For JWT authentication, this returns an empty string as passwords
     * are not stored in the security context.
     *
     * @return An empty string.
     */
    @Override
    public String getPassword() {
        return "";
    }

    /**
     * Returns the username used to authenticate the user.
     * In this implementation, the email is used as the username.
     *
     * @return The user's email address.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

}
