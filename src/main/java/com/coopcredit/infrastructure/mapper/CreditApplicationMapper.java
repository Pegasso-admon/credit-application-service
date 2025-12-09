package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.infrastructure.persistence.entity.CreditApplicationEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {
        AffiliateMapper.class })
public interface CreditApplicationMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "affiliate", source = "affiliate")
    CreditApplication toDomain(CreditApplicationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "applicationDate", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "riskEvaluation", ignore = true)
    @Mapping(target = "decisionReason", ignore = true)
    @Mapping(target = "affiliate", ignore = true)
    CreditApplication toDomain(com.coopcredit.infrastructure.controller.dto.CreditApplicationRequest request);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "affiliate", source = "affiliate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CreditApplicationEntity toEntity(CreditApplication domain);

    List<CreditApplication> toDomainList(List<CreditApplicationEntity> entities);

    default CreditApplicationEntity.ApplicationStatusEntity mapStatusToEntity(ApplicationStatus status) {
        if (status == null)
            return null;
        return switch (status) {
            case PENDING -> CreditApplicationEntity.ApplicationStatusEntity.PENDING;
            case APPROVED -> CreditApplicationEntity.ApplicationStatusEntity.APPROVED;
            case REJECTED -> CreditApplicationEntity.ApplicationStatusEntity.REJECTED;
            case CANCELLED -> CreditApplicationEntity.ApplicationStatusEntity.CANCELLED;
        };
    }

    default ApplicationStatus mapStatusToDomain(CreditApplicationEntity.ApplicationStatusEntity statusEntity) {
        if (statusEntity == null)
            return null;
        return switch (statusEntity) {
            case PENDING -> ApplicationStatus.PENDING;
            case APPROVED -> ApplicationStatus.APPROVED;
            case REJECTED -> ApplicationStatus.REJECTED;
            case CANCELLED -> ApplicationStatus.CANCELLED;
        };
    }
}
