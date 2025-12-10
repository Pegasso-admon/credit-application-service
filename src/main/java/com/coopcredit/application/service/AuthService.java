package com.coopcredit.application.service;

import com.coopcredit.domain.model.User;
import com.coopcredit.domain.service.AuthPort;
import com.coopcredit.infrastructure.config.security.JwtService;
import com.coopcredit.infrastructure.mapper.UserMapper;
import com.coopcredit.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for authentication and user management.
 * Acts as an application service implementing the AuthPort domain interface.
 */
@Service
public class AuthService implements AuthPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserJpaRepository userJpaRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public User register(User user) {
        // Check if username already exists
        if (userJpaRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Hash password and recreate User with hashed password
        User userWithHashedPassword = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(hashPassword(user.getPassword()))
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .enabled(user.isEnabled())
                .build();

        var userEntity = userMapper.toEntity(userWithHashedPassword);
        var savedEntity = userJpaRepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public AuthenticationResponse authenticate(String username, String password) {
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Get authenticated user
            var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // Generate JWT token using UserDetails
            String token = jwtService.generateToken(userDetails);

            // Get user domain model for role extraction
            User user = userJpaRepository.findByUsername(username)
                    .map(userMapper::toDomain)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Manual calculation of expiration - defaults to 24 hours (86400000 ms)
            Long expiresIn = 86400000L;

            return new AuthenticationResponse(token, username, user.getRole().name(), expiresIn);
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    @Override
    public Optional<String> validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return Optional.of(username);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
