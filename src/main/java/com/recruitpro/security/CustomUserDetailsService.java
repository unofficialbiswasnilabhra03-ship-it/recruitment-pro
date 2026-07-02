package com.recruitpro.security;

import com.recruitpro.entity.User;
import com.recruitpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security calls this to load a {@link UserPrincipal} during authentication.
 * The "username" in our system is the user's email address.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Called by Spring Security's DaoAuthenticationProvider at login,
     * and by {@link JwtFilter} on every subsequent authenticated request.
     *
     * @param email the user's email (Spring Security calls the parameter "username")
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));
        return UserPrincipal.create(user);
    }

    /**
     * Used by JwtFilter when we already have the userId from the token
     * to avoid a second DB round-trip on every request.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
