package com.coopcredit.application.usecase;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;

import java.math.BigDecimal;

/**
 * Use case for registering a new credit application.
 * <p>
 * This use case handles the initial submission of a credit application
 * by an affiliate, including validation of eligibility requirements
 * and basic business rules.
 * </p>
 *
 * <h2>Preconditions:</h2>
 * <ul>
 * <li>Affiliate must exist and be ACTIVE</li>
 * <li>Affiliate must have minimum 6 months seniority</li>
 * <li>Requested amount must be greater than zero</li>
 * <li>Term must be between 1 and 360 months</li>
 * <li>Interest rate must be valid (0-100%)</li>
 * </ul>
 *
 * <h2>Postconditions:</h2>
 * <ul>
 * <li>Application is created with PENDING status</li>
 * <li>Application date is set to current timestamp</li>
 * <li>Application is ready for risk evaluation</li>
 * </ul>
 *
 * <h2>Business Rules Applied:</h2>
 * <ul>
 * <li>Only ACTIVE affiliates can apply</li>
 * <li>Minimum seniority requirement (6 months)</li>
 * <li>Payment-to-income ratio validation (40% max)</li>
 * <li>Maximum amount validation (10x salary)</li>
 * </ul>
 */
public class RegisterCreditApplicationUseCase {

    private final CreditApplicationRepositoryPort applicationRepository;
    private final AffiliateRepositoryPort affiliateRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param applicationRepository repository for credit applications
     * @param affiliateRepository   repository for affiliates
     * @throws IllegalArgumentException if any repository is null
     */
    public RegisterCreditApplicationUseCase(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository) {
        if (applicationRepository == null) {
            throw new IllegalArgumentException("ApplicationRepository cannot be null");
        }
        if (affiliateRepository == null) {
            throw new IllegalArgumentException("AffiliateRepository cannot be null");
        }
        this.applicationRepository = applicationRepository;
        this.affiliateRepository = affiliateRepository;
    }

    /**
     * Executes the credit application registration use case.
     * <p>
     * Flow:
     * <ol>
     * <li>Validate request parameters</li>
     * <li>Retrieve and validate affiliate</li>
     * <li>Validate affiliate eligibility</li>
     * <li>Create application domain object</li>
     * <li>Validate business rules (payment ratio, amount limits)</li>
     * <li>Persist application with PENDING status</li>
     * </ol>
     * </p>
     *
     * @param request the application request data
     * @return the created credit application
     * @throws IllegalArgumentException if affiliate not found
     * @throws IllegalArgumentException if affiliate is not eligible
     * @throws IllegalArgumentException if business rules are violated
     */
    public CreditApplication execute(RegisterCreditApplicationRequest request) {
        validateRequest(request);

        Affiliate affiliate = retrieveAffiliate(request.affiliateId());
        validateAffiliateEligibility(affiliate);

        CreditApplication application = buildApplicationFromRequest(request, affiliate);
        validateBusinessRules(application);

        return applicationRepository.save(application);
    }

    /**
     * Validates the request parameters.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if request is null or invalid
     */
    private void validateRequest(RegisterCreditApplicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.affiliateId() == null) {
            throw new IllegalArgumentException("Affiliate ID is required");
        }
    }

    /**
     * Retrieves the affiliate from repository.
     *
     * @param affiliateId the affiliate's ID
     * @return the affiliate
     * @throws IllegalArgumentException if affiliate not found
     */
    private Affiliate retrieveAffiliate(Long affiliateId) {
        return affiliateRepository.findById(affiliateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Affiliate with ID %d not found", affiliateId)));
    }

    /**
     * Validates that the affiliate is eligible to apply for credit.
     *
     * @param affiliate the affiliate to validate
     * @throws IllegalArgumentException if affiliate is not eligible
     */
    private void validateAffiliateEligibility(Affiliate affiliate) {
        if (!affiliate.isActive()) {
            throw new IllegalArgumentException(
                    "Only ACTIVE affiliates can apply for credit");
        }
        if (!affiliate.hasMinimumSeniority()) {
            throw new IllegalArgumentException(
                    "Affiliate must have at least 6 months of seniority");
        }
    }

    /**
     * Builds a CreditApplication domain object from the request.
     *
     * @param request   the request data
     * @param affiliate the affiliate entity
     * @return constructed CreditApplication
     */
    private CreditApplication buildApplicationFromRequest(
            RegisterCreditApplicationRequest request,
            Affiliate affiliate) {
        return CreditApplication.builder()
                .affiliate(affiliate)
                .requestedAmount(request.requestedAmount())
                .termMonths(request.termMonths())
                .interestRate(request.interestRate())
                .status(ApplicationStatus.PENDING)
                .build();
    }

    /**
     * Validates business rules for the application.
     *
     * @param application the application to validate
     * @throws IllegalArgumentException if business rules are violated
     */
    private void validateBusinessRules(CreditApplication application) {
        if (!application.hasAcceptablePaymentToIncomeRatio()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Payment-to-income ratio (%.2f%%) exceeds maximum allowed (40%%)",
                            application.calculatePaymentToIncomeRatio().multiply(BigDecimal.valueOf(100))));
        }

        if (!application.hasAcceptableAmount()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Requested amount exceeds maximum allowed (10x salary: %s)",
                            application.getAffiliate().getSalary().multiply(BigDecimal.TEN)));
        }
    }

    /**
     * Request DTO for credit application registration.
     *
     * @param affiliateId     ID of the affiliate applying
     * @param requestedAmount amount of credit requested
     * @param termMonths      repayment term in months
     * @param interestRate    annual interest rate as percentage
     */
    public record RegisterCreditApplicationRequest(
            Long affiliateId,
            BigDecimal requestedAmount,
            Integer termMonths,
            BigDecimal interestRate) {
    }
}