package com.coopcredit.infrastructure.controller;

import com.coopcredit.application.dto.CreditApplicationResponse;
import com.coopcredit.application.dto.RegisterCreditApplicationRequest;
import com.coopcredit.application.dto.RegisterCreditApplicationResponse;
import com.coopcredit.application.usecase.RegisterCreditApplicationUseCase;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for credit application operations.
 * <p>
 * This controller handles the submission, retrieval, and management of
 * credit applications. It enforces role-based access control where:
 * <ul>
 * <li>Affiliates can submit and view their own applications</li>
 * <li>Analysts can view pending applications</li>
 * <li>Admins have full access</li>
 * </ul>
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>POST /api/applications - Submit new application (AFFILIATE, ADMIN)</li>
 * <li>GET /api/applications/{id} - Get application by ID</li>
 * <li>GET /api/applications/affiliate/{affiliateId} - Get affiliate's
 * applications</li>
 * <li>GET /api/applications/status/{status} - Get applications by status
 * (ANALYST, ADMIN)</li>
 * <li>GET /api/applications - Get all applications (ADMIN)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/applications")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Credit Applications", description = "Credit application management endpoints")
public class CreditApplicationController {

    private final RegisterCreditApplicationUseCase registerApplicationUseCase;
    private final CreditApplicationRepositoryPort applicationRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param registerApplicationUseCase use case for application registration
     * @param applicationRepository      repository for application operations
     */
    public CreditApplicationController(
            RegisterCreditApplicationUseCase registerApplicationUseCase,
            CreditApplicationRepositoryPort applicationRepository) {
        this.registerApplicationUseCase = registerApplicationUseCase;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Submits a new credit application.
     * <p>
     * Accessible by AFFILIATE and ADMIN roles.
     * Validates affiliate eligibility and business rules.
     * </p>
     *
     * @param request application data
     * @return ResponseEntity with created application (201 Created)
     * @throws IllegalArgumentException if affiliate not eligible or rules violated
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AFFILIATE', 'ADMIN')")
    @Operation(summary = "Submit credit application", description = "Creates a new credit application in PENDING status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application submitted successfully", content = @Content(schema = @Schema(implementation = RegisterCreditApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Affiliate not found")
    })
    public ResponseEntity<RegisterCreditApplicationResponse> submitApplication(
            @Valid @RequestBody RegisterCreditApplicationRequest request) {

        RegisterCreditApplicationUseCase.RegisterCreditApplicationRequest useCaseRequest = new RegisterCreditApplicationUseCase.RegisterCreditApplicationRequest(
                request.affiliateId(),
                request.requestedAmount(),
                request.termMonths(),
                request.interestRate());

        CreditApplication application = registerApplicationUseCase.execute(useCaseRequest);

        RegisterCreditApplicationResponse response = new RegisterCreditApplicationResponse(
                application.getId(),
                application.getAffiliate().getDocument(),
                application.getAffiliate().getName(),
                application.getRequestedAmount(),
                application.getTermMonths(),
                application.getInterestRate(),
                application.calculateMonthlyPayment(),
                application.getApplicationDate(),
                application.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a credit application by ID.
     * <p>
     * Accessible by all authenticated users.
     * In production, should validate user can access this application.
     * </p>
     *
     * @param id the application's unique identifier
     * @return ResponseEntity with application data (200 OK)
     * @throws IllegalArgumentException if application not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID", description = "Retrieves credit application details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found", content = @Content(schema = @Schema(implementation = CreditApplicationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CreditApplicationResponse> getApplicationById(
            @Parameter(description = "Application ID") @PathVariable Long id) {

        CreditApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Credit application with ID %d not found", id)));

        CreditApplicationResponse response = mapToResponse(application);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all applications for a specific affiliate.
     * <p>
     * Accessible by all authenticated users.
     * Affiliates should only access their own applications (enforced in
     * production).
     * </p>
     *
     * @param affiliateId the affiliate's ID
     * @return ResponseEntity with list of applications (200 OK)
     */
    @GetMapping("/affiliate/{affiliateId}")
    @Operation(summary = "Get applications by affiliate", description = "Retrieves all applications for a specific affiliate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreditApplicationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CreditApplicationResponse>> getApplicationsByAffiliate(
            @Parameter(description = "Affiliate ID") @PathVariable Long affiliateId) {

        List<CreditApplication> applications = applicationRepository.findByAffiliateId(affiliateId);

        List<CreditApplicationResponse> responses = applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all applications with a specific status.
     * <p>
     * Only accessible by ANALYST and ADMIN roles.
     * Used by analysts to find pending applications for evaluation.
     * </p>
     *
     * @param status the application status to filter by
     * @return ResponseEntity with list of applications (200 OK)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Get applications by status", description = "Retrieves all applications with specified status (ANALYST, ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreditApplicationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<CreditApplicationResponse>> getApplicationsByStatus(
            @Parameter(description = "Application status") @PathVariable ApplicationStatus status) {

        List<CreditApplication> applications = applicationRepository.findByStatus(status);

        List<CreditApplicationResponse> responses = applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all credit applications.
     * <p>
     * Only accessible by ADMIN role.
     * Should implement pagination in production.
     * </p>
     *
     * @return ResponseEntity with list of all applications (200 OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all applications", description = "Retrieves all credit applications (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreditApplicationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<CreditApplicationResponse>> getAllApplications() {
        List<CreditApplication> applications = applicationRepository.findAll();

        List<CreditApplicationResponse> responses = applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Maps CreditApplication domain object to response DTO.
     *
     * @param application the domain object
     * @return CreditApplicationResponse DTO
     */
    private CreditApplicationResponse mapToResponse(CreditApplication application) {
        return new CreditApplicationResponse(
                application.getId(),
                application.getAffiliate().getId(),
                application.getAffiliate().getDocument(),
                application.getAffiliate().getName(),
                application.getRequestedAmount(),
                application.getTermMonths(),
                application.getInterestRate(),
                application.calculateMonthlyPayment(),
                application.getApplicationDate(),
                application.getStatus(),
                application.getRiskEvaluation() != null ? application.getRiskEvaluation().getScore() : null,
                application.getRiskEvaluation() != null ? application.getRiskEvaluation().getRiskLevel().name() : null,
                application.getDecisionReason());
    }
}