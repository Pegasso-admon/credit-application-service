package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * RegisterUserRequest DTO.
 * <p>
 * Data transfer object for user registration in the authentication system.
 * </p>
 *
 * @param username unique username for login
 * @param password plain text password (will be hashed)
 * @param email    user's email address
 * @param role     user's authorization role
 */
public record RegisterUserRequest(
        @NotBlank(message = "Username is required") @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters") String username,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @NotNull(message = "Role is required") Role role) {
}
