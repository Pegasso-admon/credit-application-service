package com.coopcredit.infrastructure.controller;

import com.coopcredit.application.dto.AffiliateResponse;
import com.coopcredit.application.dto.RegisterAffiliateRequest;
import com.coopcredit.application.dto.RegisterAffiliateResponse;
import com.coopcredit.application.dto.UpdateAffiliateRequest;
import com.coopcredit.application.usecase.RegisterAffiliateUseCase;
import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for affiliate management operations.
 * <p>
 * This controller provides endpoints for CRUD operations on affiliates,
 * including registration, retrieval, and updates. It enforces role-based
 * access control for different operations.
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li>POST /api/affiliates - Register new affiliate (ADMIN, ANALYST)</li>
 *   <li>GET /api/affiliates/{id} - Get affiliate by ID (Authenticated)</li>
 *   <li>GET /api/affiliates/document/{document} - Get by document (Authenticated)</li>
 *   <li>PUT /api/affiliates/{id} - Update affiliate (ADMIN, ANALYST)</li>
 *   <li>DELETE /api/affiliates/{id} - Delete affiliate (ADMIN only)</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * <ul>
 *   <li>All endpoints require authentication</li>
 *   <li>Role-specific permissions enforced via @PreAuthorize</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/affiliates")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Affiliates", description = "Affiliate management endpoints")
public class AffiliateController {
    
    private final RegisterAffiliateUseCase registerAffiliateUseCase;
    private final AffiliateRepositoryPort affiliateRepository;
    
    /**
     * Constructor with dependency injection.
     *
     * @param registerAffiliateUseCase use case for affiliate registration
     * @param affiliateRepository repository for affiliate operations
     */
    public AffiliateController(
            RegisterAffiliateUseCase registerAffiliateUseCase,
            AffiliateRepositoryPort affiliateRepository) {
        this.registerAffiliateUseCase = registerAffiliateUseCase;
        this.affiliateRepository = affiliateRepository;
    }
    
    /**
     * Registers a new affiliate.
     * <p>
     * Only accessible by ADMIN and ANALYST roles.
     * Validates document uniqueness and business rules.
     * </p>
     *
     * @param request affiliate registration data
     * @return ResponseEntity with registered affiliate (201 Created)
     * @throws IllegalArgumentException if document already exists or data is invalid
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "Register new affiliate",
        description = "Creates a new affiliate in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Affiliate successfully registered",
            content = @Content(schema = @Schema(implementation = RegisterAffiliateResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<RegisterAffiliateResponse> registerAffiliate(
            @Valid @RequestBody RegisterAffiliateRequest request) {
        
        RegisterAffiliateUseCase.RegisterAffiliateRequest useCaseRequest =
            new RegisterAffiliateUseCase.RegisterAffiliateRequest(
                request.document(),
                request.name(),
                request.salary(),
                request.affiliationDate(),
                null
            );
        
        Affiliate affiliate = registerAffiliateUseCase.execute(useCaseRequest);
        
        RegisterAffiliateResponse response = new RegisterAffiliateResponse(
            affiliate.getId(),
            affiliate.getDocument(),
            affiliate.getName(),
            affiliate.getSalary(),
            affiliate.getAffiliationDate(),
            affiliate.getStatus(),
            affiliate.getMonthsSinceAffiliation(),
            affiliate.canApplyForCredit()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves an affiliate by ID.
     * <p>
     * Accessible by all authenticated users.
     * </p>
     *
     * @param id the affiliate's unique identifier
     * @return ResponseEntity with affiliate data (200 OK)
     * @throws IllegalArgumentException if affiliate not found
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get affiliate by ID",
        description = "Retrieves affiliate information by unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Affiliate found",
            content = @Content(schema = @Schema(implementation = AffiliateResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Affiliate not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AffiliateResponse> getAffiliateById(
            @Parameter(description = "Affiliate ID") @PathVariable Long id) {
        
        Affiliate affiliate = affiliateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Affiliate with ID %d not found", id)
                ));
        
        AffiliateResponse response = mapToResponse(affiliate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves an affiliate by document number.
     * <p>
     * Accessible by all authenticated users.
     * </p>
     *
     * @param document the affiliate's identification document
     * @return ResponseEntity with affiliate data (200 OK)
     * @throws IllegalArgumentException if affiliate not found
     */
    @GetMapping("/document/{document}")
    @Operation(
        summary = "Get affiliate by document",
        description = "Retrieves affiliate information by document number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Affiliate found",
            content = @Content(schema = @Schema(implementation = AffiliateResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Affiliate not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AffiliateResponse> getAffiliateByDocument(
            @Parameter(description = "Document number") @PathVariable String document) {
        
        Affiliate affiliate = affiliateRepository.findByDocument(document)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Affiliate with document %s not found", document)
                ));
        
        AffiliateResponse response = mapToResponse(affiliate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates an existing affiliate.
     * <p>
     * Only accessible by ADMIN and ANALYST roles.
     * Allows partial updates (only provided fields are updated).
     * </p>
     *
     * @param id the affiliate's ID
     * @param request update data
     * @return ResponseEntity with updated affiliate (200 OK)
     * @throws IllegalArgumentException if affiliate not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "Update affiliate",
        description = "Updates affiliate information (partial update)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Affiliate updated successfully",
            content = @Content(schema = @Schema(implementation = AffiliateResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Affiliate not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<AffiliateResponse> updateAffiliate(
            @Parameter(description = "Affiliate ID") @PathVariable Long id,
            @Valid @RequestBody UpdateAffiliateRequest request) {
        
        Affiliate existing = affiliateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Affiliate with ID %d not found", id)
                ));
        
        Affiliate updated = Affiliate.builder()
                .id(existing.getId())
                .document(existing.getDocument())
                .name(request.name() != null ? request.name() : existing.getName())
                .salary(request.salary() != null ? request.salary() : existing.getSalary())
                .affiliationDate(existing.getAffiliationDate())
                .status(request.status() != null ? request.status() : existing.getStatus())
                .build();
        
        Affiliate saved = affiliateRepository.save(updated);
        
        AffiliateResponse response = mapToResponse(saved);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deletes an affiliate by ID.
     * <p>
     * Only accessible by ADMIN role.
     * Consider using soft deletion in production.
     * </p>
     *
     * @param id the affiliate's ID
     * @return ResponseEntity with no content (204 No Content)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete affiliate",
        description = "Deletes an affiliate from the system (ADMIN only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Affiliate deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Affiliate not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteAffiliate(
            @Parameter(description = "Affiliate ID") @PathVariable Long id) {
        
        affiliateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Maps Affiliate domain object to response DTO.
     *
     * @param affiliate the domain object
     * @return AffiliateResponse DTO
     */
    private AffiliateResponse mapToResponse(Affiliate affiliate) {
        return new AffiliateResponse(
            affiliate.getId(),
            affiliate.getDocument(),
            affiliate.getName(),
            affiliate.getSalary(),
            affiliate.getAffiliationDate(),
            affiliate.getStatus(),
            affiliate.getMonthsSinceAffiliation(),
            affiliate.canApplyForCredit()
        );
    }
}