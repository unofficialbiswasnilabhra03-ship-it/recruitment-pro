package com.recruitpro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
    private String password;

    @Size(max = 20)
    private String phone;

    /**
     * Accepted values: ROLE_HR | ROLE_INTERVIEWER | ROLE_CANDIDATE
     * ROLE_ADMIN is seeded via DataInitializer, not self-registerable.
     */
    @NotBlank(message = "Role is required")
    private String role;
}
