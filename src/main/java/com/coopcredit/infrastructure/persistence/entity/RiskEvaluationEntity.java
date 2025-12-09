package com.coopcredit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a risk evaluation for a credit application.
 * Maps to the 'risk_evaluations' table in the database.
 * Contains one-to-one relationship with CreditApplicationEntity.
 */
@Entity
@Table(name = "risk_evaluations", indexes = {
        @Index(name = "idx_risk_level", columnList = "riskLevel"),
        @Index(name = "idx_risk_score", columnList = "score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_application_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_risk_credit_application"))
    private CreditApplicationEntity creditApplication;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RiskLevelEntity riskLevel;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Column(nullable = false)
    private Boolean meetsMinimumSeniority;

    @Column(nullable = false)
    private Boolean meetsMaximumAmount;

    @Column(nullable = false)
    private Boolean approved;

    @Column(length = 500)
    private String evaluationDetail;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum representing risk levels.
     */
    public enum RiskLevelEntity {
        LOW,
        MEDIUM,
        HIGH
    }
}