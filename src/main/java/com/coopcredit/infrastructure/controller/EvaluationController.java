package com.coopcredit.infrastructure.controller;

import com.coopcredit.application.dto.EvaluationResponse;
import com.coopcredit.application.usecase.EvaluateCreditApplicationUseCase;
import com.coopcredit.domain.model.CreditApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for credit application evaluation operations.
 * <p>
 * This controller handles the credit evaluation process, which includes:
 * <ul>
 *   <li>Calling external risk-central service</li>
 *   <li>Applying internal credit policies</li>
 *   <li>Making approval/rejection decisions</li>
 *   <li>Updating application status</li>
 * </ul>
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li>POST /api/evaluations/{applicationId} - Evaluate application (ANALYST, ADMIN)</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * <ul>
 *   <li>Only ANALYST and ADMIN roles can evaluate applications</li>
 *   <li>Applications must be in PENDING status</li>
 *   <li>Evaluation is transactional and atomic</li>
 * </ul>
 *
 * <h2>Business Process:</h2>
 * <pre>
 *   1. Validate application is PENDING
 *   2. Call external risk-central service
 *   3. Evaluate risk score and level
 *   4. Apply internal policies:
 *      - Payment-to-income ratio ≤ 40%
 *      - Amount ≤ 10x salary
 *      - Minimum seniority ≥ 6 months
 *   5. Decide APPROVED or REJECTED
 *   6. Update application atomically
 * </pre>
 */
@RestController
@RequestMapping("/api/evaluations")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Credit Evaluation", description = "Credit application evaluation endpoints")
public class EvaluationController {
    
    private final EvaluateCreditApplicationUseCase evaluateUseCase;
    
    /**
     * Constructor with dependency injection.
     *
     * @param evaluateUseCase use case for credit evaluation
     */
    public EvaluationController(EvaluateCreditApplicationUseCase evaluateUseCase) {
        this.evaluateUseCase = evaluateUseCase;
    }
    
    /**
     * Evaluates a credit application.
     * <p>
     * This endpoint orchestrates the complete evaluation process:
     * <ol>
     *   <li>Retrieves the application (must be PENDING)</li>
     *   <li>Calls external risk-central microservice</li>
     *   <li>Creates risk evaluation record</li>
     *   <li>Applies internal credit policies</li>
     *   <li>Decides approval or rejection</li>
     *   <li>Updates application status transactionally</li>
     * </ol>
     * </p>
     *
     * <h3>Evaluation Criteria:</h3>
     * <ul>
     *   <li><b>HIGH risk (score 300-500):</b> Automatic rejection</li>
     *   <li><b>MEDIUM risk (score 501-700):</b> Requires perfect compliance</li>
     *   <li><b>LOW risk (score 701-950):</b> Approved if basic requirements met</li>
     *   <li><b>Payment-to-income ratio:</b> Must not exceed 40%</li>
     *   <li><b>Maximum amount:</b> Cannot exceed 10x monthly salary</li>
     *   <li><b>Minimum seniority:</b> At least 6 months required</li>
     * </ul>
     *
     * @param applicationId the ID of the application to evaluate
     * @return ResponseEntity with evaluation result (200 OK)
     * @throws IllegalArgumentException if application not found or cannot be evaluated
     * @throws RuntimeException if external risk service fails
     */
    @PostMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
        summary = "Evaluate credit application",
        description = "Performs complete credit evaluation including risk assessment and policy validation"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application evaluated successfully",
            content = @Content(schema = @Schema(implementation = EvaluationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Application cannot be evaluated (not PENDING or ineligible affiliate)"
        ),
        @ApiResponse(responseCode = "404", description = "Application not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
        @ApiResponse(
            responseCode = "500", 
            description = "External risk service unavailable"
        )
    })
    public ResponseEntity<EvaluationResponse> evaluateApplication(
            @Parameter(
                description = "ID of the credit application to evaluate",
                required = true
            ) @PathVariable Long applicationId) {
        
        EvaluateCreditApplicationUseCase.EvaluationResult result = 
            evaluateUseCase.execute(applicationId);
        
        CreditApplication application = result.application();
        
        EvaluationResponse response = new EvaluationResponse(
            application.getId(),
            application.getAffiliate().getDocument(),
            application.getAffiliate().getName(),
            application.getRequestedAmount(),
            application.getTermMonths(),
            application.calculateMonthlyPayment(),
            application.getStatus(),
            result.approved(),
            result.reason(),
            application.getRiskEvaluation().getScore(),
            application.getRiskEvaluation().getRiskLevel().name(),
            application.getRiskEvaluation().getDetail(),
            application.calculatePaymentToIncomeRatio(),
            application.getRiskEvaluation().getEvaluatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
}