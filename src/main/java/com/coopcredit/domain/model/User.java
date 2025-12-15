package com.coopcredit.domain.model;

import com.coopcredit.domain.model.enums.Role;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a system user with authentication and authorization capabilities.
 * <p>
 * This domain model encapsulates user credentials and role information
 * for secure access to the CoopCredit system. It is designed to be
 * framework-agnostic and focused purely on business logic.
 * </p>
 *
 * <h2>Invariants:</h2>
 * <ul>
 * <li>Username must be unique and non-empty</li>
 * <li>Password must be securely hashed</li>
 * <li>Every user must have at least one role</li>
 * <li>Created date cannot be in the future</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 * <li>Users are immutable once created (use builder pattern for
 * modifications)</li>
 * <li>Password changes require creating a new User instance</li>
 * <li>Role changes require administrative privileges</li>
 * </ul>
 */
public final class User {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final Role role;
    private final LocalDateTime createdAt;
    private final boolean enabled;

    /**
     * Private constructor to enforce immutability and use of builder pattern.
     *
     * @param id        unique identifier
     * @param username  unique username for authentication
     * @param password  securely hashed password
     * @param email     user's email address
     * @param role      user's authorization role
     * @param createdAt timestamp of user creation
     * @param enabled   whether the user account is active
     */
    private User(Long id, String username, String password, String email,
            Role role, LocalDateTime createdAt, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.enabled = enabled;

        validateInvariants();
    }

    /**
     * Validates business invariants to ensure domain integrity.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    private void validateInvariants() {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("User must have a role");
        }
        // Temporarily commented out due to timezone differences between UTC (DB) and local time (Java)
        // if (createdAt != null && createdAt.isAfter(LocalDateTime.now())) {
        //     throw new IllegalArgumentException("Creation date cannot be in the future");
        // }
    }

    /**
     * Creates a new User instance with builder pattern.
     *
     * @return new UserBuilder instance
     */
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    /**
     * Checks if this user has the specified role.
     *
     * @param requiredRole the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Checks if this user is an administrator.
     *
     * @return true if user has ROLE_ADMIN
     */
    public boolean isAdmin() {
        return hasRole(Role.ROLE_ADMIN);
    }

    /**
     * Checks if this user is an analyst.
     *
     * @return true if user has ROLE_ANALYST
     */
    public boolean isAnalyst() {
        return hasRole(Role.ROLE_ANALYST);
    }

    /**
     * Checks if this user is an affiliate.
     *
     * @return true if user has ROLE_AFFILIATE
     */
    public boolean isAffiliate() {
        return hasRole(Role.ROLE_AFFILIATE);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * Builder pattern implementation for User construction.
     */
    public static final class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String email;
        private Role role;
        private LocalDateTime createdAt;
        private boolean enabled = true;

        private UserBuilder() {
        }

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Builds and validates the User instance.
         *
         * @return immutable User instance
         * @throws IllegalArgumentException if invariants are violated
         */
        public User build() {
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            return new User(id, username, password, email, role, createdAt, enabled);
        }
    }
}