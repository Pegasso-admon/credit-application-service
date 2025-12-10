package com.coopcredit.application.usecase;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.model.enums.RiskLevel;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;
import com.coopcredit.domain.service.RiskEvaluationPort;
import org.springframework.stereotype.Service;

/**
 * Use case for evaluating a credit application.
 * <p>
 * This is the core use case that orchestrates the complete credit evaluation
 * process, including external risk assessment, internal policy validation,
 * and final decision making. This operation must be transactional.
 * </p>
 *
 * <h2>Preconditions:</h2>
 * <ul>
 * <li>Application must exist and be in PENDING status</li>
 * <li>Affiliate must be ACTIVE with minimum seniority</li>
 * <li>External risk-central service must be available</li>
 * </ul>
 *
 * <h2>Postconditions:</h2>
 * <ul>
 * <li>Risk evaluation is persisted with the application</li>
 * <li>Application status is updated to APPROVED or REJECTED</li>
 * <li>Decision reason is recorded</li>
 * <li>All changes are atomic (transactional)</li>
 * </ul>
 *
 * <h2>Evaluation Flow:</h2>
 * 
 * <pre>
 *   1. Validate application can be evaluated (PENDING status)
 *   2. Call external risk-central service
 *   3. Create RiskEvaluation domain object
 *   4. Apply internal policies:
 *      - Payment-to-income ratio (≤40%)
 *      - Amount limit (≤10x salary)
 *      - Minimum seniority (≥6 months)
 *      - Risk level acceptability
 *   5. Determine APPROVED or REJECTED
 *   6. Update application atomically
 * </pre>
 *
 * <h2>Business Rules Applied:</h2>
 * <ul>
 * <li>HIGH risk → Automatic rejection</li>
 * <li>MEDIUM risk → Requires perfect compliance with all other rules</li>
 * <li>LOW risk → Approved if basic requirements met</li>
 * <li>Payment-to-income ratio must not exceed 40%</li>
 * <li>Requested amount must not exceed 10x monthly salary</li>
 * </ul>
 */
@Service
public class EvaluateCreditApplicationUseCase {

        private final CreditApplicationRepositoryPort applicationRepository;
        private final RiskEvaluationPort riskEvaluationPort;

        /**
         * Constructor with dependency injection.
         *
         * @param applicationRepository repository for credit applications
         * @param riskEvaluationPort    service for external risk evaluation
         * @throws IllegalArgumentException if any dependency is null
         */
        public EvaluateCreditApplicationUseCase(
                        CreditApplicationRepositoryPort applicationRepository,
                        RiskEvaluationPort riskEvaluationPort) {
                if (applicationRepository == null) {
                        throw new IllegalArgumentException("ApplicationRepository cannot be null");
                }
                if (riskEvaluationPort == null) {
                        throw new IllegalArgumentException("RiskEvaluationPort cannot be null");
                }
                this.applicationRepository = applicationRepository;
                this.riskEvaluationPort = riskEvaluationPort;
        }

        /**
         * Executes the credit application evaluation use case.
         * <p>
         * This method must be executed within a transactional context
         * to ensure atomicity of the evaluation process.
         * </p>
         *
         * @param applicationId the ID of the application to evaluate
         * @return the evaluation result with updated application
         * @throws IllegalArgumentException if application not found
         * @throws IllegalArgumentException if application cannot be evaluated
         * @throws RuntimeException         if external service fails
         */
        public EvaluationResult execute(Long applicationId) {
                validateApplicationId(applicationId);

                CreditApplication application = retrieveApplication(applicationId);
                validateCanBeEvaluated(application);

                RiskEvaluation riskEvaluation = callExternalRiskService(application);

                EvaluationDecision decision = evaluateApplication(application, riskEvaluation);

                CreditApplication updatedApplication = buildUpdatedApplication(
                                application,
                                riskEvaluation,
                                decision);

                CreditApplication savedApplication = applicationRepository.save(updatedApplication);

                // Return updatedApplication (which has riskEvaluation in memory) instead of
                // savedApplication
                // The savedApplication comes from DB mapping which doesn't include
                // riskEvaluation relationship
                CreditApplication resultApplication = updatedApplication.toBuilder()
                                .id(savedApplication.getId())
                                .build();

                return new EvaluationResult(
                                resultApplication,
                                decision.approved(),
                                decision.reason());
        }

        /**
         * Validates the application ID.
         *
         * @param applicationId the ID to validate
         * @throws IllegalArgumentException if ID is null
         */
        private void validateApplicationId(Long applicationId) {
                if (applicationId == null) {
                        throw new IllegalArgumentException("Application ID cannot be null");
                }
        }

        /**
         * Retrieves the application from repository.
         *
         * @param applicationId the application ID
         * @return the credit application
         * @throws IllegalArgumentException if not found
         */
        private CreditApplication retrieveApplication(Long applicationId) {
                return applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                String.format("Credit application with ID %d not found",
                                                                applicationId)));
        }

        /**
         * Validates that the application can be evaluated.
         *
         * @param application the application to validate
         * @throws IllegalArgumentException if cannot be evaluated
         */
        private void validateCanBeEvaluated(CreditApplication application) {
                if (!application.canBeEvaluated()) {
                        throw new IllegalArgumentException(
                                        String.format(
                                                        "Application cannot be evaluated. Status: %s, Affiliate can apply: %s",
                                                        application.getStatus(),
                                                        application.getAffiliate().canApplyForCredit()));
                }
        }

        /**
         * Calls the external risk-central service.
         *
         * @param application the application to evaluate
         * @return external risk evaluation response
         * @throws RuntimeException if service fails
         */
        private RiskEvaluation callExternalRiskService(
                        CreditApplication application) {
                return riskEvaluationPort.evaluateRisk(
                                application.getAffiliate().getDocument(),
                                application.getRequestedAmount(),
                                application.getTermMonths());
        }

        /**
         * Evaluates the application against all business rules.
         *
         * @param application    the application to evaluate
         * @param riskEvaluation the risk evaluation result
         * @return evaluation decision with reason
         */
        private EvaluationDecision evaluateApplication(
                        CreditApplication application,
                        RiskEvaluation riskEvaluation) {

                if (riskEvaluation.isHighRisk()) {
                        return new EvaluationDecision(
                                        false,
                                        String.format("High risk level detected (score: %d)",
                                                        riskEvaluation.getScore()));
                }

                if (!application.getAffiliate().canApplyForCredit()) {
                        return new EvaluationDecision(
                                        false,
                                        "Affiliate does not meet eligibility requirements");
                }

                if (!application.hasAcceptablePaymentToIncomeRatio()) {
                        return new EvaluationDecision(
                                        false,
                                        String.format(
                                                        "Payment-to-income ratio (%.2f%%) exceeds maximum (40%%)",
                                                        application.calculatePaymentToIncomeRatio().multiply(
                                                                        java.math.BigDecimal.valueOf(100))));
                }

                if (!application.hasAcceptableAmount()) {
                        return new EvaluationDecision(
                                        false,
                                        "Requested amount exceeds maximum allowed (10x monthly salary)");
                }

                return new EvaluationDecision(
                                true,
                                String.format(
                                                "Approved - Risk level: %s, Score: %d, Payment ratio: %.2f%%",
                                                riskEvaluation.getRiskLevel(),
                                                riskEvaluation.getScore(),
                                                application.calculatePaymentToIncomeRatio().multiply(
                                                                java.math.BigDecimal.valueOf(100))));
        }

        /**
         * Builds an updated application with evaluation results.
         *
         * @param original       the original application
         * @param riskEvaluation the risk evaluation
         * @param decision       the evaluation decision
         * @return new application instance with updates
         */
        private CreditApplication buildUpdatedApplication(
                        CreditApplication original,
                        RiskEvaluation riskEvaluation,
                        EvaluationDecision decision) {
                return CreditApplication.builder()
                                .id(original.getId())
                                .affiliate(original.getAffiliate())
                                .requestedAmount(original.getRequestedAmount())
                                .termMonths(original.getTermMonths())
                                .interestRate(original.getInterestRate())
                                .applicationDate(original.getApplicationDate())
                                .status(decision.approved() ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED)
                                .riskEvaluation(riskEvaluation)
                                .decisionReason(decision.reason())
                                .build();
        }

        /**
         * Internal record for evaluation decision.
         */
        private record EvaluationDecision(boolean approved, String reason) {
        }

        /**
         * Result DTO for evaluation use case.
         *
         * @param application the evaluated application
         * @param approved    whether the application was approved
         * @param reason      the decision reason
         */
        public record EvaluationResult(
                        CreditApplication application,
                        boolean approved,
                        String reason) {
        }
}