package com.coopcredit.domain.model;

import com.coopcredit.domain.model.enums.RiskLevel;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the risk evaluation result for a credit application.
 * <p>
 * This domain model encapsulates the risk assessment data obtained from
 * external credit bureaus and internal evaluation policies. It is used to
 * determine the creditworthiness of an affiliate.
 * </p>
 *
 * <h2>Invariants:</h2>
 * <ul>
 * <li>Score must be between 300 and 950</li>
 * <li>Risk level must correspond to score range</li>
 * <li>Evaluation date cannot be in the future</li>
 * <li>Evaluation reason must be provided if rejected</li>
 * </ul>
 *
 * <h2>Score Ranges:</h2>
 * <ul>
 * <li>300-500: HIGH risk</li>
 * <li>501-700: MEDIUM risk</li>
 * <li>701-950: LOW risk</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 * <li>Risk evaluation is obtained from external risk-central service</li>
 * <li>Score consistency must be maintained for same document number</li>
 * <li>Evaluation is immutable once created</li>
 * </ul>
 */
public final class RiskEvaluation {

    private static final int MIN_SCORE = 300;
    private static final int MAX_SCORE = 950;
    private static final int HIGH_RISK_THRESHOLD = 500;
    private static final int MEDIUM_RISK_THRESHOLD = 700;

    private final Long id;
    private final Integer score;
    private final RiskLevel riskLevel;
    private final String detail;
    private final LocalDateTime evaluatedAt;
    private final boolean approved;
    private final String rejectionReason;

    /**
     * Private constructor to enforce immutability and use of builder pattern.
     *
     * @param id              unique identifier
     * @param score           credit score from risk bureau (300-950)
     * @param riskLevel       calculated risk level
     * @param detail          additional details from risk evaluation
     * @param evaluatedAt     timestamp of evaluation
     * @param approved        whether the risk evaluation passed internal policies
     * @param rejectionReason reason for rejection if not approved
     */
    private RiskEvaluation(Long id, Integer score, RiskLevel riskLevel, String detail,
            LocalDateTime evaluatedAt, boolean approved, String rejectionReason) {
        this.id = id;
        this.score = score;
        this.riskLevel = riskLevel;
        this.detail = detail;
        this.evaluatedAt = evaluatedAt;
        this.approved = approved;
        this.rejectionReason = rejectionReason;

        validateInvariants();
    }

    /**
     * Validates business invariants to ensure domain integrity.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    private void validateInvariants() {
        if (score == null || score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    String.format("Score must be between %d and %d", MIN_SCORE, MAX_SCORE));
        }
        if (riskLevel == null) {
            throw new IllegalArgumentException("Risk level cannot be null");
        }
        if (!isScoreConsistentWithRiskLevel()) {
            throw new IllegalArgumentException("Score does not match risk level");
        }
        if (evaluatedAt != null && evaluatedAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Evaluation date cannot be in the future");
        }
        if (!approved && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException("Rejection reason must be provided when not approved");
        }
    }

    /**
     * Validates that the score matches the assigned risk level.
     *
     * @return true if score and risk level are consistent
     */
    private boolean isScoreConsistentWithRiskLevel() {
        return switch (riskLevel) {
            case HIGH -> score >= MIN_SCORE && score <= HIGH_RISK_THRESHOLD;
            case MEDIUM -> score > HIGH_RISK_THRESHOLD && score <= MEDIUM_RISK_THRESHOLD;
            case LOW -> score > MEDIUM_RISK_THRESHOLD && score <= MAX_SCORE;
        };
    }

    /**
     * Creates a new RiskEvaluation instance with builder pattern.
     *
     * @return new RiskEvaluationBuilder instance
     */
    public static RiskEvaluationBuilder builder() {
        return new RiskEvaluationBuilder();
    }

    /**
     * Determines the risk level based on score.
     * <p>
     * This is a pure domain function that applies business rules
     * for risk classification.
     * </p>
     *
     * @param score the credit score
     * @return corresponding risk level
     * @throws IllegalArgumentException if score is out of valid range
     */
    public static RiskLevel determineRiskLevel(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    String.format("Score must be between %d and %d", MIN_SCORE, MAX_SCORE));
        }

        if (score <= HIGH_RISK_THRESHOLD) {
            return RiskLevel.HIGH;
        } else if (score <= MEDIUM_RISK_THRESHOLD) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * Checks if this evaluation represents an acceptable risk level.
     * <p>
     * Business rule: HIGH risk is typically unacceptable unless
     * additional guarantees are provided.
     * </p>
     *
     * @return true if risk level is LOW or MEDIUM
     */
    public boolean isAcceptableRisk() {
        return riskLevel == RiskLevel.LOW || riskLevel == RiskLevel.MEDIUM;
    }

    /**
     * Checks if this evaluation represents high risk.
     *
     * @return true if risk level is HIGH
     */
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH;
    }

    public Long getId() {
        return id;
    }

    public Integer getScore() {
        return score;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RiskEvaluation that = (RiskEvaluation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RiskEvaluation{" +
                "id=" + id +
                ", score=" + score +
                ", riskLevel=" + riskLevel +
                ", approved=" + approved +
                '}';
    }

    /**
     * Builder pattern implementation for RiskEvaluation construction.
     */
    public static final class RiskEvaluationBuilder {
        private Long id;
        private Integer score;
        private RiskLevel riskLevel;
        private String detail;
        private LocalDateTime evaluatedAt;
        private boolean approved;
        private String rejectionReason;

        private RiskEvaluationBuilder() {
        }

        public RiskEvaluationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RiskEvaluationBuilder score(Integer score) {
            this.score = score;
            return this;
        }

        public RiskEvaluationBuilder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public RiskEvaluationBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public RiskEvaluationBuilder evaluatedAt(LocalDateTime evaluatedAt) {
            this.evaluatedAt = evaluatedAt;
            return this;
        }

        public RiskEvaluationBuilder approved(boolean approved) {
            this.approved = approved;
            return this;
        }

        public RiskEvaluationBuilder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        /**
         * Builds and validates the RiskEvaluation instance.
         * Automatically determines risk level from score if not set.
         *
         * @return immutable RiskEvaluation instance
         * @throws IllegalArgumentException if invariants are violated
         */
        public RiskEvaluation build() {
            if (evaluatedAt == null) {
                evaluatedAt = LocalDateTime.now();
            }
            if (riskLevel == null && score != null) {
                riskLevel = determineRiskLevel(score);
            }
            return new RiskEvaluation(id, score, riskLevel, detail, evaluatedAt, approved, rejectionReason);
        }
    }
}