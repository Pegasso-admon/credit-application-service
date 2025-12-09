package com.coopcredit.domain.service;

import com.coopcredit.domain.model.User;

import java.util.Optional;

/**
 * Service port for authentication and user management operations.
 * <p>
 * This port defines the contract for user authentication, authorization,
 * and credential management without coupling to specific security frameworks.
 * </p>
 *
 * <h2>Hexagonal Architecture:</h2>
 * <p>
 * This is an OUTPUT port (driven/secondary) that will be implemented
 * by infrastructure adapters using Spring Security, JWT, or other
 * authentication mechanisms.
 * </p>
 *
 * <h2>Security Requirements:</h2>
 * <ul>
 * <li>Passwords must be hashed (never stored in plain text)</li>
 * <li>Usernames must be unique</li>
 * <li>Failed authentication attempts should be logged</li>
 * <li>Tokens should have expiration times</li>
 * </ul>
 */
public interface AuthPort {

    /**
     * Registers a new user in the system.
     * <p>
     * The password will be hashed before storage.
     * Username uniqueness must be validated.
     * </p>
     *
     * @param user the user to register (password in plain text)
     * @return the registered user with hashed password
     * @throws IllegalArgumentException if username already exists
     * @throws IllegalArgumentException if user data is invalid
     */
    User register(User user);

    /**
     * Authenticates a user and generates an access token.
     * <p>
     * Validates credentials and creates a JWT or similar token
     * for subsequent requests.
     * </p>
     *
     * @param username the username
     * @param password the password (plain text)
     * @return AuthenticationResponse containing token and user details
     * @throws IllegalArgumentException if credentials are invalid
     */
    AuthenticationResponse authenticate(String username, String password);

    /**
     * Retrieves a user by username.
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Hashes a plain text password.
     * <p>
     * Uses a secure hashing algorithm like BCrypt.
     * </p>
     *
     * @param plainPassword the password in plain text
     * @return the hashed password
     */
    String hashPassword(String plainPassword);

    /**
     * Verifies if a plain password matches a hashed password.
     *
     * @param plainPassword  the password in plain text
     * @param hashedPassword the hashed password
     * @return true if passwords match
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);

    /**
     * Validates a token and returns the associated username.
     *
     * @param token the JWT or authentication token
     * @return Optional containing username if token is valid
     */
    Optional<String> validateToken(String token);

    /**
     * Data transfer object for authentication responses.
     */
    record AuthenticationResponse(
            String token,
            String username,
            String role,
            Long expiresIn) {
        /**
         * Creates a new AuthenticationResponse.
         *
         * @param token     the access token (JWT)
         * @param username  the authenticated username
         * @param role      the user's role
         * @param expiresIn token expiration time in milliseconds
         */
        public AuthenticationResponse {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
        }
    }
}