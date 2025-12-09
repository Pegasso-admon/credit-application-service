package com.coopcredit.domain.model;

import com.coopcredit.domain.model.enums.AffiliateStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents an affiliate member of the CoopCredit cooperative.
 * <p>
 * An affiliate is a registered member who can apply for credit products.
 * This domain model encapsulates all affiliate-related business rules and
 * validations without any infrastructure dependencies.
 * </p>
 *
 * <h2>Invariants:</h2>
 * <ul>
 * <li>Document number must be unique and non-empty</li>
 * <li>Name must be non-empty</li>
 * <li>Salary must be greater than zero</li>
 * <li>Affiliation date cannot be in the future</li>
 * <li>Only ACTIVE affiliates can apply for credit</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 * <li>Minimum seniority of 6 months required for credit applications</li>
 * <li>Maximum credit amount is calculated based on salary multipliers</li>
 * <li>Status changes require administrative approval</li>
 * </ul>
 */
public final class Affiliate {

    private static final int MINIMUM_SENIORITY_MONTHS = 6;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Long id;
    private final String document;
    private final String name;
    private final BigDecimal salary;
    private final LocalDate affiliationDate;
    private final AffiliateStatus status;

    /**
     * Private constructor to enforce immutability and use of builder pattern.
     *
     * @param id              unique identifier
     * @param document        unique identification document
     * @param name            full name of the affiliate
     * @param salary          monthly salary
     * @param affiliationDate date when affiliate joined the cooperative
     * @param status          current status (ACTIVE/INACTIVE)
     */
    private Affiliate(Long id, String document, String name, BigDecimal salary,
            LocalDate affiliationDate, AffiliateStatus status) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.salary = salary;
        this.affiliationDate = affiliationDate;
        this.status = status;

        validateInvariants();
    }

    /**
     * Validates business invariants to ensure domain integrity.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    private void validateInvariants() {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("Document cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (salary == null || salary.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Salary must be greater than zero");
        }
        if (affiliationDate == null) {
            throw new IllegalArgumentException("Affiliation date cannot be null");
        }
        if (affiliationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Affiliation date cannot be in the future");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Creates a new Affiliate instance with builder pattern.
     *
     * @return new AffiliateBuilder instance
     */
    public static AffiliateBuilder builder() {
        return new AffiliateBuilder();
    }

    /**
     * Checks if the affiliate is active and eligible for credit applications.
     *
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return status == AffiliateStatus.ACTIVE;
    }

    /**
     * Checks if the affiliate meets the minimum seniority requirement.
     * <p>
     * Business rule: Affiliates must have at least 6 months of seniority
     * to be eligible for credit applications.
     * </p>
     *
     * @return true if affiliate has minimum required seniority
     */
    public boolean hasMinimumSeniority() {
        long monthsSinceAffiliation = ChronoUnit.MONTHS.between(affiliationDate, LocalDate.now());
        return monthsSinceAffiliation >= MINIMUM_SENIORITY_MONTHS;
    }

    /**
     * Validates if the affiliate is eligible to apply for credit.
     * <p>
     * Requirements:
     * <ul>
     * <li>Must be ACTIVE</li>
     * <li>Must have minimum seniority (6 months)</li>
     * </ul>
     * </p>
     *
     * @return true if eligible, false otherwise
     */
    public boolean canApplyForCredit() {
        return isActive() && hasMinimumSeniority();
    }

    /**
     * Calculates the maximum credit amount this affiliate can request.
     * <p>
     * Business rule: Maximum credit is typically 10x monthly salary
     * for low-risk affiliates.
     * </p>
     *
     * @param multiplier the salary multiplier to apply
     * @return maximum allowed credit amount
     */
    public BigDecimal calculateMaxCreditAmount(int multiplier) {
        return salary.multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Gets the number of months since affiliation.
     *
     * @return months of seniority
     */
    public long getMonthsSinceAffiliation() {
        return ChronoUnit.MONTHS.between(affiliationDate, LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public String getDocument() {
        return document;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public LocalDate getAffiliationDate() {
        return affiliationDate;
    }

    public AffiliateStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Affiliate affiliate = (Affiliate) o;
        return Objects.equals(document, affiliate.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document);
    }

    @Override
    public String toString() {
        return "Affiliate{" +
                "id=" + id +
                ", document='" + document + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * Builder pattern implementation for Affiliate construction.
     */
    public static final class AffiliateBuilder {
        private Long id;
        private String document;
        private String name;
        private BigDecimal salary;
        private LocalDate affiliationDate;
        private AffiliateStatus status = AffiliateStatus.ACTIVE;

        private AffiliateBuilder() {
        }

        public AffiliateBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AffiliateBuilder document(String document) {
            this.document = document;
            return this;
        }

        public AffiliateBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AffiliateBuilder salary(BigDecimal salary) {
            this.salary = salary;
            return this;
        }

        public AffiliateBuilder affiliationDate(LocalDate affiliationDate) {
            this.affiliationDate = affiliationDate;
            return this;
        }

        public AffiliateBuilder status(AffiliateStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Builds and validates the Affiliate instance.
         *
         * @return immutable Affiliate instance
         * @throws IllegalArgumentException if invariants are violated
         */
        public Affiliate build() {
            if (affiliationDate == null) {
                affiliationDate = LocalDate.now();
            }
            return new Affiliate(id, document, name, salary, affiliationDate, status);
        }
    }
}