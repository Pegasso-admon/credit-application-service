package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.Role;

/**
 * UserResponse DTO.
 * <p>
 * Generic user information response.
 * </p>
 *
 * @param id       user ID
 * @param username username
 * @param email    email address
 * @param role     user role
 * @param enabled  whether account is active
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Boolean enabled) {
}
