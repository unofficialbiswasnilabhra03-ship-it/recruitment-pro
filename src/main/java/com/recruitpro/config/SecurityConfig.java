package com.recruitpro.config;

import com.recruitpro.security.JwtAuthenticationEntryPoint;
import com.recruitpro.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize / @PostAuthorize on service/controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;
    private final UserDetailsService userDetailsService;

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ── Security filter chain ─────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Stateless REST API — no CSRF, no sessions
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(jwtEntryPoint))

            .authorizeHttpRequests(auth -> auth

                // ── Public endpoints ─────────────────────────────────────────
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                ).permitAll()

                // Public read-only job browsing (candidates can search without login)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/jobs",
                        "/api/v1/jobs/{id}",
                        "/api/v1/companies",
                        "/api/v1/companies/{id}"
                ).permitAll()

                // ── Auth-protected: any authenticated user ───────────────────
                .requestMatchers("/api/v1/users/me/**").authenticated()
                .requestMatchers("/api/v1/notifications/**").authenticated()

                // ── Candidate-only ───────────────────────────────────────────
                .requestMatchers(HttpMethod.POST,  "/api/v1/applications").hasRole("CANDIDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/applications/{id}/withdraw").hasRole("CANDIDATE")
                .requestMatchers("/api/v1/candidates/me/**").hasRole("CANDIDATE")
                .requestMatchers("/api/v1/resumes/me/**").hasRole("CANDIDATE")

                // ── HR-only ──────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST,   "/api/v1/jobs").hasRole("HR")
                .requestMatchers(HttpMethod.PUT,    "/api/v1/jobs/**").hasRole("HR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/**").hasRole("HR")
                .requestMatchers(HttpMethod.POST,   "/api/v1/companies").hasRole("HR")
                .requestMatchers(HttpMethod.PUT,    "/api/v1/companies/**").hasRole("HR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/companies/**").hasRole("HR")
                .requestMatchers("/api/v1/applications/{id}/status").hasRole("HR")
                .requestMatchers("/api/v1/interviews").hasAnyRole("HR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/interviews").hasAnyRole("HR", "ADMIN")

                // ── Interviewer-only ─────────────────────────────────────────
                .requestMatchers("/api/v1/interviews/{id}/feedback").hasRole("INTERVIEWER")

                // ── Dashboard: HR + Admin ────────────────────────────────────
                .requestMatchers("/api/v1/dashboard/**").hasAnyRole("HR", "ADMIN")

                // ── Admin-only ───────────────────────────────────────────────
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users").hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── CORS ──────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // tighten in prod via env var
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
