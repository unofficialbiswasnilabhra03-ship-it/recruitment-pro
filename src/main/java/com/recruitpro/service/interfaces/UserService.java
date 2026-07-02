package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.UpdateUserRequest;
import com.recruitpro.dto.response.UserResponse;
import com.recruitpro.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getCurrentUser(Long userId);

    UserResponse updateCurrentUser(Long userId, UpdateUserRequest request);

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> getUsersByRole(RoleName role, Pageable pageable);

    Page<UserResponse> searchUsers(String query, Pageable pageable);

    void enableUser(Long id);

    void disableUser(Long id);

    void deleteUser(Long id);
}
