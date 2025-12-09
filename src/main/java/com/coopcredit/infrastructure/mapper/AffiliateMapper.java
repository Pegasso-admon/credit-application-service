package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.enums.AffiliateStatus;
import com.coopcredit.infrastructure.controller.dto.AffiliateRequest;
import com.coopcredit.infrastructure.persistence.entity.AffiliateEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AffiliateMapper {

    @Mapping(target = "status", source = "status")
    Affiliate toDomain(AffiliateEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Affiliate toDomain(AffiliateRequest request);

    /**
     * Converts Affiliate domain model to AffiliateEntity.
     *
     * @param domain the Affiliate domain model to convert
     * @return the AffiliateEntity
     */
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AffiliateEntity toEntity(Affiliate domain);

    List<Affiliate> toDomainList(List<AffiliateEntity> entities);

    default AffiliateEntity.AffiliateStatusEntity mapStatusToEntity(AffiliateStatus status) {
        if (status == null)
            return null;
        return switch (status) {
            case ACTIVE -> AffiliateEntity.AffiliateStatusEntity.ACTIVE;
            case INACTIVE -> AffiliateEntity.AffiliateStatusEntity.INACTIVE;
            case SUSPENDED -> AffiliateEntity.AffiliateStatusEntity.SUSPENDED;
        };
    }

    default AffiliateStatus mapStatusToDomain(AffiliateEntity.AffiliateStatusEntity statusEntity) {
        if (statusEntity == null)
            return null;
        return switch (statusEntity) {
            case ACTIVE -> AffiliateStatus.ACTIVE;
            case INACTIVE -> AffiliateStatus.INACTIVE;
            case SUSPENDED -> AffiliateStatus.SUSPENDED;
        };
    }
}
