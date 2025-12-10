package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.model.enums.RiskLevel;
import com.coopcredit.infrastructure.persistence.entity.RiskEvaluationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RiskEvaluationMapper.
 * Tests entity ↔ domain conversions and risk level mappings.
 */
@DisplayName("RiskEvaluationMapper Tests")
class RiskEvaluationMapperTest {

    private final RiskEvaluationMapper mapper = new RiskEvaluationMapperImpl();

    @Test
    @DisplayName("Should convert RiskEvaluationEntity to Domain - LOW risk")
    void shouldConvertEntityToDomainWithLowRisk() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setId(1L);
        entity.setCreditApplicationId(100L);
        entity.setScore(850);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.LOW);
        entity.setDebtToIncomeRatio(new BigDecimal("25.5"));
        entity.setMeetsMinimumSeniority(true);
        entity.setMeetsMaximumAmount(true);
        entity.setApproved(true);
        entity.setEvaluationDetail("Excellent credit history");
        entity.setEvaluatedAt(now);

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getScore()).isEqualTo(850);
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(domain.getDebtToIncomeRatio()).isEqualByComparingTo(new BigDecimal("25.5"));
        assertThat(domain.isMeetsMinimumSeniority()).isTrue();
        assertThat(domain.isMeetsMaximumAmount()).isTrue();
        assertThat(domain.isApproved()).isTrue();
        assertThat(domain.getDetail()).isEqualTo("Excellent credit history");
        assertThat(domain.getEvaluatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should convert RiskEvaluationEntity to Domain - MEDIUM risk")
    void shouldConvertEntityToDomainWithMediumRisk() {
        // Given
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setScore(650);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.MEDIUM);
        entity.setDebtToIncomeRatio(new BigDecimal("40.0"));
        entity.setMeetsMinimumSeniority(true);
        entity.setMeetsMaximumAmount(true);
        entity.setApproved(true);
        entity.setEvaluationDetail("Moderate risk profile");
        entity.setEvaluatedAt(LocalDateTime.now());

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(domain.getScore()).isEqualTo(650);
    }

    @Test
    @DisplayName("Should convert RiskEvaluationEntity to Domain - HIGH risk")
    void shouldConvertEntityToDomainWithHighRisk() {
        // Given
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setScore(400);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.HIGH);
        entity.setDebtToIncomeRatio(new BigDecimal("60.0"));
        entity.setMeetsMinimumSeniority(false);
        entity.setMeetsMaximumAmount(true);
        entity.setApproved(false);
        entity.setEvaluationDetail("High risk profile");
        entity.setEvaluatedAt(LocalDateTime.now());

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(domain.isApproved()).isFalse();
        assertThat(domain.isMeetsMinimumSeniority()).isFalse();
    }

    @Test
    @DisplayName("Should convert Domain to Entity - LOW risk")
    void shouldConvertDomainToEntityWithLowRisk() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RiskEvaluation domain = RiskEvaluation.builder()
                .id(1L)
                .score(850)
                .riskLevel(RiskLevel.LOW)
                .debtToIncomeRatio(new BigDecimal("25.5"))
                .meetsMinimumSeniority(true)
                .meetsMaximumAmount(true)
                .approved(true)
                .detail("Excellent credit history")
                .evaluatedAt(now)
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getScore()).isEqualTo(850);
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.LOW);
        assertThat(entity.getDebtToIncomeRatio()).isEqualByComparingTo(new BigDecimal("25.5"));
        assertThat(entity.isMeetsMinimumSeniority()).isTrue();
        assertThat(entity.isMeetsMaximumAmount()).isTrue();
        assertThat(entity.isApproved()).isTrue();
        assertThat(entity.getEvaluationDetail()).isEqualTo("Excellent credit history");
    }

    @Test
    @DisplayName("Should convert Domain to Entity - MEDIUM risk")
    void shouldConvertDomainToEntityWithMediumRisk() {
        // Given
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(650)
                .riskLevel(RiskLevel.MEDIUM)
                .debtToIncomeRatio(new BigDecimal("40.0"))
                .meetsMinimumSeniority(true)
                .meetsMaximumAmount(true)
                .approved(true)
                .detail("Moderate risk")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.MEDIUM);
    }

    @Test
    @DisplayName("Should convert Domain to Entity - HIGH risk")
    void shouldConvertDomainToEntityWithHighRisk() {
        // Given
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(400)
                .riskLevel(RiskLevel.HIGH)
                .debtToIncomeRatio(new BigDecimal("60.0"))
                .meetsMinimumSeniority(false)
                .meetsMaximumAmount(true)
                .approved(false)
                .detail("High risk")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.HIGH);
        assertThat(entity.isApproved()).isFalse();
    }

    @Test
    @DisplayName("Should preserve all fields during round-trip conversion")
    void shouldPreserveFieldsInRoundTrip() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RiskEvaluation originalDomain = RiskEvaluation.builder()
                .id(99L)
                .score(750)
                .riskLevel(RiskLevel.MEDIUM)
                .debtToIncomeRatio(new BigDecimal("35.75"))
                .meetsMinimumSeniority(true)
                .meetsMaximumAmount(true)
                .approved(true)
                .detail("Test risk evaluation")
                .evaluatedAt(now)
                .build();

        // When - Convert to entity and back to domain
        RiskEvaluationEntity entity = mapper.toEntity(originalDomain);
        RiskEvaluation resultDomain = mapper.toDomain(entity);

        // Then
        assertThat(resultDomain).isNotNull();
        assertThat(resultDomain.getId()).isEqualTo(originalDomain.getId());
        assertThat(resultDomain.getScore()).isEqualTo(originalDomain.getScore());
        assertThat(resultDomain.getRiskLevel()).isEqualTo(originalDomain.getRiskLevel());
        assertThat(resultDomain.getDebtToIncomeRatio()).isEqualByComparingTo(originalDomain.getDebtToIncomeRatio());
        assertThat(resultDomain.isMeetsMinimumSeniority()).isEqualTo(originalDomain.isMeetsMinimumSeniority());
        assertThat(resultDomain.isMeetsMaximumAmount()).isEqualTo(originalDomain.isMeetsMaximumAmount());
        assertThat(resultDomain.isApproved()).isEqualTo(originalDomain.isApproved());
        assertThat(resultDomain.getDetail()).isEqualTo(originalDomain.getDetail());
    }

    @Test
    @DisplayName("Should handle null entity")
    void shouldHandleNullEntity() {
        // Given
        RiskEvaluationEntity entity = null;

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle null domain")
    void shouldHandleNullDomain() {
        // Given
        RiskEvaluation domain = null;

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should correctly map all three risk levels bidirectionally")
    void shouldMapAllRiskLevelsBidirectionally() {
        // Test LOW
        assertRiskLevelMapping(RiskLevel.LOW, RiskEvaluationEntity.RiskLevelEntity.LOW);

        // Test MEDIUM
        assertRiskLevelMapping(RiskLevel.MEDIUM, RiskEvaluationEntity.RiskLevelEntity.MEDIUM);

        // Test HIGH
        assertRiskLevelMapping(RiskLevel.HIGH, RiskEvaluationEntity.RiskLevelEntity.HIGH);
    }

    private void assertRiskLevelMapping(RiskLevel domainLevel, RiskEvaluationEntity.RiskLevelEntity entityLevel) {
        // Domain to Entity
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(700)
                .riskLevel(domainLevel)
                .debtToIncomeRatio(BigDecimal.valueOf(30))
                .meetsMinimumSeniority(true)
                .meetsMaximumAmount(true)
                .approved(true)
                .detail("Test")
                .evaluatedAt(LocalDateTime.now())
                .build();

        RiskEvaluationEntity entity = mapper.toEntity(domain);
        assertThat(entity.getRiskLevel()).isEqualTo(entityLevel);

        // Entity to Domain
        RiskEvaluationEntity testEntity = new RiskEvaluationEntity();
        testEntity.setScore(700);
        testEntity.setRiskLevel(entityLevel);
        testEntity.setDebtToIncomeRatio(BigDecimal.valueOf(30));
        testEntity.setMeetsMinimumSeniority(true);
        testEntity.setMeetsMaximumAmount(true);
        testEntity.setApproved(true);
        testEntity.setEvaluationDetail("Test");
        testEntity.setEvaluatedAt(LocalDateTime.now());

        RiskEvaluation resultDomain = mapper.toDomain(testEntity);
        assertThat(resultDomain.getRiskLevel()).isEqualTo(domainLevel);
    }
}
