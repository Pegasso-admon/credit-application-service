package com.coopcredit.domain.model;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a credit application submitted by an affiliate.
 * <p>
 * This is the central domain aggregate that orchestrates the entire credit
 * application lifecycle, from submission through evaluation to final decision.
 * It encapsulates all business rules and validations related to credit
 * requests.
 * </p>
 *
 * <h2>Invariants:</h2>
 * <ul>
 * <li>Affiliate must be ACTIVE</li>
 * <li>Requested amount must be greater than zero</li>
 * <li>Term must be between 1 and 360 months</li>
 * <li>Interest rate must be between 0 and 100 percent</li>
 * <li>Application date cannot be in the future</li>
 * <li>Only PENDING applications can be evaluated</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 * <li>Monthly payment cannot exceed 40% of affiliate's salary</li>
 * <li>Requested amount cannot exceed 10x affiliate's monthly salary</li>
 * <li>Affiliate must have minimum 6 months seniority</li>
 * <li>Risk evaluation must be performed before approval</li>
 * <li>Status transitions are immutable (PENDING → APPROVED/REJECTED)</li>
 * </ul>
 *
 * <h2>Lifecycle:</h2>
 * 
 * <pre>
 *   1. Created in PENDING status
 *   2. Risk evaluation performed
 *   3. Internal policies validated
 *   4. Status changed to APPROVED or REJECTED
 * </pre>
 */
public final class CreditApplication {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MIN_TERM_MONTHS = 1;
    private static final int MAX_TERM_MONTHS = 360;
    private static final BigDecimal MAX_PAYMENT_TO_INCOME_RATIO = BigDecimal.valueOf(0.40);
    private static final int MAX_AMOUNT_SALARY_MULTIPLIER = 10;

    private final Long id;
    private final Affiliate affiliate;
    private final BigDecimal requestedAmount;
    private final Integer termMonths;
    private final BigDecimal interestRate;
    private final LocalDateTime applicationDate;
    private final ApplicationStatus status;
    private final RiskEvaluation riskEvaluation;
    private final String decisionReason;

    /**
     * Private constructor to enforce immutability and use of builder pattern.
     *
     * @param id              unique identifier
     * @param affiliate       the affiliate submitting the application
     * @param requestedAmount the amount of credit requested
     * @param termMonths      the term in months for repayment
     * @param interestRate    the proposed annual interest rate
     * @param applicationDate when the application was submitted
     * @param status          current status of the application
     * @param riskEvaluation  associated risk evaluation
     * @param decisionReason  reason for approval/rejection
     */
    private CreditApplication(Long id, Affiliate affiliate, BigDecimal requestedAmount,
            Integer termMonths, BigDecimal interestRate, LocalDateTime applicationDate,
            ApplicationStatus status, RiskEvaluation riskEvaluation, String decisionReason) {
        this.id = id;
        this.affiliate = affiliate;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.applicationDate = applicationDate;
        this.status = status;
        this.riskEvaluation = riskEvaluation;
        this.decisionReason = decisionReason;

        validateInvariants();
    }

    /**
     * Validates business invariants to ensure domain integrity.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    private void validateInvariants() {
        if (affiliate == null) {
            throw new IllegalArgumentException("Affiliate cannot be null");
        }
        if (!affiliate.isActive()) {
            throw new IllegalArgumentException("Affiliate must be ACTIVE to apply for credit");
        }
        if (requestedAmount == null || requestedAmount.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Requested amount must be greater than zero");
        }
        if (termMonths == null || termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
            throw new IllegalArgumentException(
                    String.format("Term must be between %d and %d months", MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
        if (interestRate == null || interestRate.compareTo(ZERO) < 0 || interestRate.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Interest rate must be between 0 and 100");
        }
        if (applicationDate != null && applicationDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Application date cannot be in the future");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (status != ApplicationStatus.PENDING && decisionReason == null) {
            throw new IllegalArgumentException("Decision reason required for non-pending applications");
        }
    }

    /**
     * Creates a new CreditApplication instance with builder pattern.
     *
     * @return new CreditApplicationBuilder instance
     */
    public static CreditApplicationBuilder builder() {
        return new CreditApplicationBuilder();
    }

    /**
     * Calculates the monthly payment amount for this credit application.
     * <p>
     * Uses the standard loan payment formula:
     * M = P * [r(1+r)^n] / [(1+r)^n - 1]
     * where:
     * M = monthly payment
     * P = principal (requested amount)
     * r = monthly interest rate
     * n = number of months
     * </p>
     *
     * @return monthly payment amount
     */
    public BigDecimal calculateMonthlyPayment() {
        if (interestRate.compareTo(ZERO) == 0) {
            return requestedAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = interestRate.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusRate.pow(termMonths);

        BigDecimal numerator = requestedAmount.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the payment-to-income ratio.
     * <p>
     * Business rule: This ratio should not exceed 40% for approval.
     * </p>
     *
     * @return ratio of monthly payment to affiliate's salary
     */
    public BigDecimal calculatePaymentToIncomeRatio() {
        BigDecimal monthlyPayment = calculateMonthlyPayment();
        return monthlyPayment.divide(affiliate.getSalary(), 4, RoundingMode.HALF_UP);
    }

    /**
     * Validates if the payment-to-income ratio is acceptable.
     * <p>
     * Business rule: Monthly payment cannot exceed 40% of salary.
     * </p>
     *
     * @return true if ratio is acceptable
     */
    public boolean hasAcceptablePaymentToIncomeRatio() {
        return calculatePaymentToIncomeRatio().compareTo(MAX_PAYMENT_TO_INCOME_RATIO) <= 0;
    }

    /**
     * Validates if the requested amount is within acceptable limits.
     * <p>
     * Business rule: Requested amount cannot exceed 10x monthly salary.
     * </p>
     *
     * @return true if amount is acceptable
     */
    public boolean hasAcceptableAmount() {
        BigDecimal maxAmount = affiliate.getSalary().multiply(BigDecimal.valueOf(MAX_AMOUNT_SALARY_MULTIPLIER));
        return requestedAmount.compareTo(maxAmount) <= 0;
    }

    /**
     * Checks if this application can be evaluated.
     * <p>
     * Requirements:
     * <ul>
     * <li>Status must be PENDING</li>
     * <li>Affiliate must be active</li>
     * <li>Affiliate must have minimum seniority</li>
     * </ul>
     * </p>
     *
     * @return true if application can be evaluated
     */
    public boolean canBeEvaluated() {
        return status == ApplicationStatus.PENDING &&
                affiliate.canApplyForCredit();
    }

    /**
     * Validates all business rules for approval.
     * <p>
     * Checks:
     * <ul>
     * <li>Affiliate eligibility</li>
     * <li>Payment-to-income ratio</li>
     * <li>Requested amount limits</li>
     * <li>Risk evaluation approval</li>
     * </ul>
     * </p>
     *
     * @return true if all rules pass
     */
    public boolean meetsApprovalCriteria() {
        if (!affiliate.canApplyForCredit()) {
            return false;
        }
        if (!hasAcceptablePaymentToIncomeRatio()) {
            return false;
        }
        if (!hasAcceptableAmount()) {
            return false;
        }
        if (riskEvaluation != null && !riskEvaluation.isApproved()) {
            return false;
        }
        return true;
    }

    /**
     * Checks if application is in PENDING status.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }

    /**
     * Checks if application is APPROVED.
     *
     * @return true if status is APPROVED
     */
    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }

    /**
     * Checks if application is REJECTED.
     *
     * @return true if status is REJECTED
     */
    public boolean isRejected() {
        return status == ApplicationStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public Affiliate getAffiliate() {
        return affiliate;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public RiskEvaluation getRiskEvaluation() {
        return riskEvaluation;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CreditApplication that = (CreditApplication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CreditApplication{" +
                "id=" + id +
                ", affiliate=" + affiliate.getDocument() +
                ", requestedAmount=" + requestedAmount +
                ", status=" + status +
                '}';
    }

    /**
     * Builder pattern implementation for CreditApplication construction.
     */
    public static final class CreditApplicationBuilder {
        private Long id;
        private Affiliate affiliate;
        private BigDecimal requestedAmount;
        private Integer termMonths;
        private BigDecimal interestRate;
        private LocalDateTime applicationDate;
        private ApplicationStatus status = ApplicationStatus.PENDING;
        private RiskEvaluation riskEvaluation;
        private String decisionReason;

        private CreditApplicationBuilder() {
        }

        public CreditApplicationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CreditApplicationBuilder affiliate(Affiliate affiliate) {
            this.affiliate = affiliate;
            return this;
        }

        public CreditApplicationBuilder requestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public CreditApplicationBuilder termMonths(Integer termMonths) {
            this.termMonths = termMonths;
            return this;
        }

        public CreditApplicationBuilder interestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public CreditApplicationBuilder applicationDate(LocalDateTime applicationDate) {
            this.applicationDate = applicationDate;
            return this;
        }

        public CreditApplicationBuilder status(ApplicationStatus status) {
            this.status = status;
            return this;
        }

        public CreditApplicationBuilder riskEvaluation(RiskEvaluation riskEvaluation) {
            this.riskEvaluation = riskEvaluation;
            return this;
        }

        public CreditApplicationBuilder decisionReason(String decisionReason) {
            this.decisionReason = decisionReason;
            return this;
        }

        /**
         * Builds and validates the CreditApplication instance.
         *
         * @return immutable CreditApplication instance
         * @throws IllegalArgumentException if invariants are violated
         */
        public CreditApplication build() {
            if (applicationDate == null) {
                applicationDate = LocalDateTime.now();
            }
            return new CreditApplication(id, affiliate, requestedAmount, termMonths,
                    interestRate, applicationDate, status,
                    riskEvaluation, decisionReason);
        }
    }

    /**
     * Creates a builder initialized with the current instance values.
     *
     * @return initialized Builder
     */
    public CreditApplicationBuilder toBuilder() {
        return new CreditApplicationBuilder()
                .id(this.id)
                .affiliate(this.affiliate)
                .requestedAmount(this.requestedAmount)
                .termMonths(this.termMonths)
                .interestRate(this.interestRate)
                .applicationDate(this.applicationDate)
                .status(this.status)
                .riskEvaluation(this.riskEvaluation)
                .decisionReason(this.decisionReason);
    }
}