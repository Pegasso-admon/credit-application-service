package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.enums.RiskLevel;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.infrastructure.persistence.entity.RiskEvaluationEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between RiskEvaluation domain model and
 * RiskEvaluationEntity.
 * Handles bidirectional mapping with proper enum conversion.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RiskEvaluationMapper {

    /**
     * Converts RiskEvaluationEntity to RiskEvaluation domain model.
     *
     * @param entity the RiskEvaluationEntity to convert
     * @return the RiskEvaluation domain model
     */
    @Mapping(target = "riskLevel", source = "riskLevel")
    RiskEvaluation toDomain(RiskEvaluationEntity entity);

    /**
     * Converts RiskEvaluation domain model to RiskEvaluationEntity.
     *
     * @param domain the RiskEvaluation domain model to convert
     * @return the RiskEvaluationEntity
     */
    @Mapping(target = "riskLevel", source = "riskLevel")
    @Mapping(target = "creditApplication", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RiskEvaluationEntity toEntity(RiskEvaluation domain);

    /**
     * Converts list of RiskEvaluationEntity to list of RiskEvaluation domain
     * models.
     *
     * @param entities the list of RiskEvaluationEntity
     * @return the list of RiskEvaluation domain models
     */
    List<RiskEvaluation> toDomainList(List<RiskEvaluationEntity> entities);

    /**
     * Maps RiskLevel enum from domain to entity.
     *
     * @param riskLevel the domain RiskLevel
     * @return the entity RiskLevelEntity
     */
    default RiskEvaluationEntity.RiskLevelEntity mapRiskLevelToEntity(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return null;
        }
        return switch (riskLevel) {
            case LOW -> RiskEvaluationEntity.RiskLevelEntity.LOW;
            case MEDIUM -> RiskEvaluationEntity.RiskLevelEntity.MEDIUM;
            case HIGH -> RiskEvaluationEntity.RiskLevelEntity.HIGH;
        };
    }

    /**
     * Maps RiskLevelEntity enum from entity to domain.
     *
     * @param riskLevelEntity the entity RiskLevelEntity
     * @return the domain RiskLevel
     */
    default RiskLevel mapRiskLevelToDomain(RiskEvaluationEntity.RiskLevelEntity riskLevelEntity) {
        if (riskLevelEntity == null) {
            return null;
        }
        return switch (riskLevelEntity) {
            case LOW -> RiskLevel.LOW;
            case MEDIUM -> RiskLevel.MEDIUM;
            case HIGH -> RiskLevel.HIGH;
        };
    }

    /**
     * Updates an existing RiskEvaluationEntity with values from RiskEvaluation
     * domain model.
     *
     * @param domain the RiskEvaluation domain model
     * @param entity the RiskEvaluationEntity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creditApplication", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDomain(RiskEvaluation domain, @MappingTarget RiskEvaluationEntity entity);
}