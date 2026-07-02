package com.recruitpro.security;

import com.recruitpro.constants.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every HTTP request, extracts the JWT from the Authorization header,
 * validates it, and populates {@link SecurityContextHolder} so that downstream
 * {@code @PreAuthorize} annotations and {@code Principal} injection work correctly.
 *
 * Flow:
 *  1. Extract "Bearer <token>" from Authorization header
 *  2. Validate token signature and expiry via JwtUtil
 *  3. Load UserPrincipal by userId from token claims (avoids email DB lookup)
 *  4. Set authentication in SecurityContextHolder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Don't propagate — let the request continue unauthenticated.
            // JwtAuthenticationEntryPoint handles the 401 if a protected route is hit.
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AppConstants.AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(AppConstants.TOKEN_PREFIX)) {
            return header.substring(AppConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
