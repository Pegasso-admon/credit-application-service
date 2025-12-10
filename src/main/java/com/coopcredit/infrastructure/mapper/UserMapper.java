package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.enums.Role;
import com.coopcredit.domain.model.User;
import com.coopcredit.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between User domain model and UserEntity.
 * Handles bidirectional mapping with proper enum conversion.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Converts UserEntity to User domain model.
     *
     * @param entity the UserEntity to convert
     * @return the User domain model
     */
    @Mapping(target = "enabled", source = "active")
    @Mapping(target = "role", source = "role")
    User toDomain(UserEntity entity);

    /**
     * Converts User domain model to UserEntity.
     *
     * @param domain the User domain model to convert
     * @return the UserEntity
     */
    @Mapping(target = "active", source = "enabled")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(User domain);

    /**
     * Maps Role enum from domain to entity.
     *
     * @param role the domain Role
     * @return the entity RoleEntity
     */
    default UserEntity.RoleEntity mapRoleToEntity(Role role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case ROLE_AFFILIATE -> UserEntity.RoleEntity.ROLE_AFFILIATE;
            case ROLE_ANALYST -> UserEntity.RoleEntity.ROLE_ANALYST;
            case ROLE_ADMIN -> UserEntity.RoleEntity.ROLE_ADMIN;
        };
    }

    /**
     * Maps RoleEntity enum from entity to domain.
     *
     * @param roleEntity the entity RoleEntity
     * @return the domain Role
     */
    default Role mapRoleToDomain(UserEntity.RoleEntity roleEntity) {
        if (roleEntity == null) {
            return null;
        }
        return switch (roleEntity) {
            case ROLE_AFFILIATE -> Role.ROLE_AFFILIATE;
            case ROLE_ANALYST -> Role.ROLE_ANALYST;
            case ROLE_ADMIN -> Role.ROLE_ADMIN;
        };
    }

    /**
     * Updates an existing UserEntity with values from User domain model.
     *
     * @param domain the User domain model
     * @param entity the UserEntity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);
}