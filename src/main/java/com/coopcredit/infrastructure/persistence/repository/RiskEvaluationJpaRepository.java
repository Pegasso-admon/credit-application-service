package com.coopcredit.infrastructure.persistence.repository;

import com.coopcredit.infrastructure.persistence.entity.RiskEvaluationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for RiskEvaluationEntity.
 * Provides CRUD operations and custom queries for risk evaluation management.
 */
@Repository
public interface RiskEvaluationJpaRepository extends JpaRepository<RiskEvaluationEntity, Long> {

    /**
     * Finds a risk evaluation by credit application ID.
     *
     * @param creditApplicationId the credit application ID
     * @return an Optional containing the risk evaluation if found
     */
    @EntityGraph(attributePaths = { "creditApplication", "creditApplication.affiliate" })
    @Query("SELECT re FROM RiskEvaluationEntity re WHERE re.creditApplication.id = :creditApplicationId")
    Optional<RiskEvaluationEntity> findByCreditApplicationId(@Param("creditApplicationId") Long creditApplicationId);

    /**
     * Finds all risk evaluations by risk level.
     *
     * @param riskLevel the risk level to filter by
     * @return list of risk evaluations with the specified risk level
     */
    @EntityGraph(attributePaths = { "creditApplication", "creditApplication.affiliate" })
    List<RiskEvaluationEntity> findByRiskLevel(RiskEvaluationEntity.RiskLevelEntity riskLevel);

    /**
     * Finds all approved risk evaluations.
     *
     * @return list of approved risk evaluations
     */
    @EntityGraph(attributePaths = { "creditApplication", "creditApplication.affiliate" })
    @Query("SELECT re FROM RiskEvaluationEntity re WHERE re.approved = true")
    List<RiskEvaluationEntity> findAllApproved();

    /**
     * Finds all rejected risk evaluations.
     *
     * @return list of rejected risk evaluations
     */
    @EntityGraph(attributePaths = { "creditApplication", "creditApplication.affiliate" })
    @Query("SELECT re FROM RiskEvaluationEntity re WHERE re.approved = false")
    List<RiskEvaluationEntity> findAllRejected();

    /**
     * Finds risk evaluations by score range.
     *
     * @param minScore minimum score
     * @param maxScore maximum score
     * @return list of risk evaluations within the score range
     */
    @EntityGraph(attributePaths = { "creditApplication", "creditApplication.affiliate" })
    @Query("SELECT re FROM RiskEvaluationEntity re WHERE re.score BETWEEN :minScore AND :maxScore")
    List<RiskEvaluationEntity> findByScoreRange(@Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore);

    /**
     * Counts evaluations by risk level.
     *
     * @param riskLevel the risk level
     * @return the count of evaluations
     */
    @Query("SELECT COUNT(re) FROM RiskEvaluationEntity re WHERE re.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") RiskEvaluationEntity.RiskLevelEntity riskLevel);
}