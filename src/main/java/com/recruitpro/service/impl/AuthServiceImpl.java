package com.recruitpro.service.impl;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.dto.request.*;
import com.recruitpro.dto.response.AuthResponse;
import com.recruitpro.entity.RefreshToken;
import com.recruitpro.entity.Role;
import com.recruitpro.entity.User;
import com.recruitpro.enums.RoleName;
import com.recruitpro.exception.BadRequestException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.exception.DuplicateResourceException;
import com.recruitpro.repository.RefreshTokenRepository;
import com.recruitpro.repository.RoleRepository;
import com.recruitpro.repository.UserRepository;
import com.recruitpro.security.JwtUtil;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.AuthService;
import com.recruitpro.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository          userRepository;
    private final RoleRepository          roleRepository;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final PasswordEncoder         passwordEncoder;
    private final AuthenticationManager   authenticationManager;
    private final JwtUtil                 jwtUtil;
    private final EmailService            emailService;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        RoleName roleName = parseRole(request.getRole());
        if (roleName == RoleName.ROLE_ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
        log.info("New user registered: {} with role {}", user.getEmail(), roleName);

        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getEmail()));

        return buildAuthResponse(user);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid or revoked refresh token"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadRequestException("Refresh token has expired — please log in again");
        }

        User user = stored.getUser();
        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(
                    LocalDateTime.now().plusMinutes(AppConstants.PASSWORD_RESET_TOKEN_EXPIRY_MINUTES));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user, token);
        });
        // Always succeed — don't reveal whether the email exists
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        // Revoke all refresh tokens so existing sessions are invalidated
        refreshTokenRepository.revokeAllByUser(user);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.create(user);

        String accessToken  = jwtUtil.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(roleName)
                .build();
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private RoleName parseRole(String role) {
        try {
            return RoleName.valueOf(role.toUpperCase().startsWith("ROLE_")
                    ? role.toUpperCase() : "ROLE_" + role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role);
        }
    }
}
