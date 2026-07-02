package com.recruitpro.service.impl;

import com.recruitpro.dto.request.UpdateUserRequest;
import com.recruitpro.dto.response.UserResponse;
import com.recruitpro.entity.User;
import com.recruitpro.enums.RoleName;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.repository.UserRepository;
import com.recruitpro.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return toResponse(findById(userId));
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        User user = findById(userId);
        if (request.getFirstName()       != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()        != null) user.setLastName(request.getLastName());
        if (request.getPhone()           != null) user.setPhone(request.getPhone());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(RoleName role, Pageable pageable) {
        return userRepository.findByRoleName(role, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        User user = findById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        User user = findById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .profilePictureUrl(u.getProfilePictureUrl())
                .roles(u.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet()))
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
