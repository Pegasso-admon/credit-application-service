package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.Role;

/**
 * AuthResponse DTO.
 * <p>
 * Response containing JWT token and user information.
 * </p>
 *
 * @param token     JWT access token
 * @param username  authenticated username
 * @param email     user's email
 * @param role      user's role
 * @param expiresIn token expiration time in milliseconds
 */
public record AuthResponse(
        String token,
        String username,
        String email,
        Role role,
        Long expiresIn) {
}
