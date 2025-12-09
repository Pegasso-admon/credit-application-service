package com.coopcredit.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest DTO.
 * <p>
 * Data transfer object for authentication requests.
 * </p>
 *
 * @param username the username
 * @param password the password in plain text
 */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,

        @NotBlank(message = "Password is required") String password) {
}
