package com.coopcredit.infrastructure.controller;

import com.coopcredit.application.dto.AuthResponse;
import com.coopcredit.application.dto.LoginRequest;
import com.coopcredit.application.dto.RegisterUserRequest;
import com.coopcredit.application.dto.UserResponse;
import com.coopcredit.domain.model.User;
import com.coopcredit.domain.service.AuthPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication and user management operations.
 * <p>
 * This controller handles user registration, login, and token-based
 * authentication. It acts as an INPUT adapter in hexagonal architecture,
 * translating HTTP requests into domain operations.
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li>POST /api/auth/register - Register new user</li>
 *   <li>POST /api/auth/login - Authenticate and get token</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * <ul>
 *   <li>These endpoints are public (no authentication required)</li>
 *   <li>Passwords are hashed before storage</li>
 *   <li>JWT tokens are returned for subsequent authenticated requests</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {
    
    private final AuthPort authPort;
    
    /**
     * Constructor with dependency injection.
     *
     * @param authPort authentication service port
     */
    public AuthController(AuthPort authPort) {
        this.authPort = authPort;
    }
    
    /**
     * Registers a new user in the system.
     * <p>
     * Creates a new user account with the specified role and credentials.
     * The password will be securely hashed before storage.
     * </p>
     *
     * @param request registration data including username, password, email, and role
     * @return ResponseEntity with created user information (201 Created)
     * @throws IllegalArgumentException if username already exists
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with specified credentials and role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or username already exists"
        )
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = User.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .role(request.role())
                .enabled(true)
                .build();
        
        User registeredUser = authPort.register(user);
        
        UserResponse response = new UserResponse(
            registeredUser.getId(),
            registeredUser.getUsername(),
            registeredUser.getEmail(),
            registeredUser.getRole(),
            registeredUser.isEnabled()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Authenticates a user and generates an access token.
     * <p>
     * Validates credentials and returns a JWT token for subsequent
     * authenticated requests. Token should be included in Authorization
     * header as "Bearer {token}".
     * </p>
     *
     * @param request login credentials (username and password)
     * @return ResponseEntity with JWT token and user information (200 OK)
     * @throws IllegalArgumentException if credentials are invalid
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user and returns JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthPort.AuthenticationResponse authResult = authPort.authenticate(
            request.username(),
            request.password()
        );
        
        User user = authPort.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        AuthResponse response = new AuthResponse(
            authResult.token(),
            authResult.username(),
            user.getEmail(),
            user.getRole(),
            authResult.expiresIn()
        );
        
        return ResponseEntity.ok(response);
    }
}