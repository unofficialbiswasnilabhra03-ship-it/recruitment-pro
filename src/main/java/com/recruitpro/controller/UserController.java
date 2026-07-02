package com.recruitpro.controller;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.dto.request.UpdateUserRequest;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.UserResponse;
import com.recruitpro.enums.RoleName;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user profile + admin user management")
public class UserController {

    private final UserService userService;

    // ── Current user (any authenticated role) ─────────────────────────────────

    @GetMapping("/users/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("User fetched",
                userService.getCurrentUser(principal.getId())));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateCurrentUser(principal.getId(), request)));
    }

    // ── Admin: list / search / manage all users ───────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (Admin)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false)     String search,
            @RequestParam(required = false)     RoleName role) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> result;
        if (search != null && !search.isBlank()) {
            result = userService.searchUsers(search, pageable);
        } else if (role != null) {
            result = userService.getUsersByRole(role, pageable);
        } else {
            result = userService.getAllUsers(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Users fetched", result));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched",
                userService.getUserById(id)));
    }

    @PatchMapping("/users/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable a user account (Admin)")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success("User enabled"));
    }

    @PatchMapping("/users/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable a user account (Admin)")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success("User disabled"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}
