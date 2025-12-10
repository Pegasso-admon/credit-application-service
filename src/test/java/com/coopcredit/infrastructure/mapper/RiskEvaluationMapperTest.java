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
 * Unit tests for RiskEvaluationMapper covering all risk level conversions.
 */
@DisplayName("RiskEvaluationMapper Tests")
class RiskEvaluationMapperTest {

    private final RiskEvaluationMapper mapper = new RiskEvaluationMapperImpl();

    @Test
    @DisplayName("Should convert Entity to Domain - LOW risk approved")
    void shouldConvertEntityToDomainLowRisk() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setId(1L);
        entity.setScore(850);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.LOW);
        entity.setApproved(true);
        entity.setEvaluationDetail("Excellent credit");
        entity.setEvaluatedAt(now);

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getScore()).isEqualTo(850);
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(domain.isApproved()).isTrue();
        assertThat(domain.getDetail()).isEqualTo("Excellent credit");
    }

    @Test
    @DisplayName("Should convert Entity to Domain - MEDIUM risk")
    void shouldConvertEntityToDomainMediumRisk() {
        // Given
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setScore(650);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.MEDIUM);
        entity.setApproved(true);
        entity.setEvaluationDetail("Moderate risk");
        entity.setEvaluatedAt(LocalDateTime.now());

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getScore()).isEqualTo(650);
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("Should convert Entity to Domain - HIGH risk rejected")
    void shouldConvertEntityToDomainHighRisk() {
        // Given
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setScore(400);
        entity.setRiskLevel(RiskEvaluationEntity.RiskLevelEntity.HIGH);
        entity.setApproved(false);
        entity.setEvaluationDetail("High risk - low score");
        entity.setEvaluatedAt(LocalDateTime.now());

        // When
        RiskEvaluation domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(domain.isApproved()).isFalse();
    }

    @Test
    @DisplayName("Should convert Domain to Entity - LOW risk")
    void shouldConvertDomainToEntityLowRisk() {
        // Given
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(800)
                .riskLevel(RiskLevel.LOW)
                .approved(true)
                .detail("Low risk profile")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getScore()).isEqualTo(800);
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.LOW);
        assertThat(entity.getApproved()).isTrue();
    }

    @Test
    @DisplayName("Should convert Domain to Entity - MEDIUM risk")
    void shouldConvertDomainToEntityMediumRisk() {
        // Given
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(650)
                .riskLevel(RiskLevel.MEDIUM)
                .approved(true)
                .detail("Medium risk")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.MEDIUM);
    }

    @Test
    @DisplayName("Should convert Domain to Entity - HIGH risk with rejection")
    void shouldConvertDomainToEntityHighRisk() {
        // Given
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(450)
                .riskLevel(RiskLevel.HIGH)
                .approved(false)
                .rejectionReason("Score too low")
                .detail("High risk evaluation")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // When
        RiskEvaluationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRiskLevel()).isEqualTo(RiskEvaluationEntity.RiskLevelEntity.HIGH);
        assertThat(entity.getApproved()).isFalse();
    }

    @Test
    @DisplayName("Should handle null entity gracefully")
    void shouldHandleNullEntity() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should handle null domain gracefully")
    void shouldHandleNullDomain() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should map all risk levels bidirectionally")
    void shouldMapAllRiskLevelsBidirectionally() {
        // LOW
        verifyRiskLevelMapping(RiskLevel.LOW, RiskEvaluationEntity.RiskLevelEntity.LOW, 800);
        // MEDIUM
        verifyRiskLevelMapping(RiskLevel.MEDIUM, RiskEvaluationEntity.RiskLevelEntity.MEDIUM, 650);
        // HIGH
        verifyRiskLevelMapping(RiskLevel.HIGH, RiskEvaluationEntity.RiskLevelEntity.HIGH, 450);
    }

    private void verifyRiskLevelMapping(RiskLevel domainLevel,
            RiskEvaluationEntity.RiskLevelEntity entityLevel,
            int score) {
        // Domain → Entity mapping
        RiskEvaluation domain = RiskEvaluation.builder()
                .score(score)
                .riskLevel(domainLevel)
                .approved(true)
                .detail("Test")
                .evaluatedAt(LocalDateTime.now())
                .build();

        RiskEvaluationEntity entity = mapper.toEntity(domain);
        assertThat(entity.getRiskLevel()).isEqualTo(entityLevel);

        // Entity → Domain mapping
        RiskEvaluationEntity testEntity = new RiskEvaluationEntity();
        testEntity.setScore(score);
        testEntity.setRiskLevel(entityLevel);
        testEntity.setApproved(true);
        testEntity.setEvaluationDetail("Test");
        testEntity.setEvaluatedAt(LocalDateTime.now());

        RiskEvaluation resultDomain = mapper.toDomain(testEntity);
        assertThat(resultDomain.getRiskLevel()).isEqualTo(domainLevel);
    }
}
