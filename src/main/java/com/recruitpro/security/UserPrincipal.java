package com.recruitpro.security;

import com.recruitpro.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps the {@link User} entity for Spring Security.
 * Keeps the entity out of the security context — only what Security needs lives here.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user) {
        this.id              = user.getId();
        this.email           = user.getEmail();
        this.password        = user.getPassword();
        this.enabled         = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
        this.authorities     = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    // ── UserDetails overrides ─────────────────────────────────────────────────

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // ── Helpers used by JwtUtil and services ─────────────────────────────────

    public List<String> getRoleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }
}
