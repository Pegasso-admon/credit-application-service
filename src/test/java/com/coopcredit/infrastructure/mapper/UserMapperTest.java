package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.User;
import com.coopcredit.domain.model.enums.Role;
import com.coopcredit.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper.
 * Tests entity ↔ domain conversions and role mappings.
 */
@DisplayName("UserMapper Tests")
class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    @DisplayName("Should convert UserEntity to Domain - ANALYST role")
    void shouldConvertEntityToDomainWithAnalystRole() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("analyst1");
        entity.setDocument("12345678");
        entity.setPassword("$2a$10$hashed");
        entity.setFullName("Maria Rodriguez");
        entity.setEmail("analyst1@example.com");
        entity.setRole(UserEntity.RoleEntity.ROLE_ANALYST);
        entity.setActive(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // When
        User domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getUsername()).isEqualTo("analyst1");
        assertThat(domain.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(domain.getEmail()).isEqualTo("analyst1@example.com");
        assertThat(domain.getRole()).isEqualTo(Role.ROLE_ANALYST);
        assertThat(domain.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should convert UserEntity to Domain - AFFILIATE role")
    void shouldConvertEntityToDomainWithAffiliateRole() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setUsername("affiliate1");
        entity.setDocument("87654321");
        entity.setPassword("$2a$10$hashed");
        entity.setFullName("Juan Perez");
        entity.setEmail("affiliate1@example.com");
        entity.setRole(UserEntity.RoleEntity.ROLE_AFFILIATE);
        entity.setActive(true);

        // When
        User domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getRole()).isEqualTo(Role.ROLE_AFFILIATE);
    }

    @Test
    @DisplayName("Should convert UserEntity to Domain - ADMIN role")
    void shouldConvertEntityToDomainWithAdminRole() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setUsername("admin");
        entity.setDocument("11111111");
        entity.setPassword("$2a$10$hashed");
        entity.setFullName("Admin User");
        entity.setEmail("admin@example.com");
        entity.setRole(UserEntity.RoleEntity.ROLE_ADMIN);
        entity.setActive(true);

        // When
        User domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should convert Domain to UserEntity - ANALYST role")
    void shouldConvertDomainToEntityWithAnalystRole() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User domain = User.builder()
                .id(1L)
                .username("analyst1")
                .password("$2a$10$hashed")
                .email("analyst1@example.com")
                .role(Role.ROLE_ANALYST)
                .enabled(true)
                .createdAt(now)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUsername()).isEqualTo("analyst1");
        assertThat(entity.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(entity.getEmail()).isEqualTo("analyst1@example.com");
        assertThat(entity.getRole()).isEqualTo(UserEntity.RoleEntity.ROLE_ANALYST);
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should convert Domain to UserEntity - AFFILIATE role")
    void shouldConvertDomainToEntityWithAffiliateRole() {
        // Given
        User domain = User.builder()
                .username("affiliate1")
                .password("$2a$10$hashed")
                .email("affiliate1@example.com")
                .role(Role.ROLE_AFFILIATE)
                .enabled(true)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRole()).isEqualTo(UserEntity.RoleEntity.ROLE_AFFILIATE);
    }

    @Test
    @DisplayName("Should convert Domain to UserEntity - ADMIN role")
    void shouldConvertDomainToEntityWithAdminRole() {
        // Given
        User domain = User.builder()
                .username("admin")
                .password("$2a$10$hashed")
                .email("admin@example.com")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getRole()).isEqualTo(UserEntity.RoleEntity.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should handle inactive user")
    void shouldHandleInactiveUser() {
        // Given
        User domain = User.builder()
                .username("inactive_user")
                .password("$2a$10$hashed")
                .email("inactive@example.com")
                .role(Role.ROLE_AFFILIATE)
                .enabled(false)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    @DisplayName("Should preserve all fields during round-trip conversion")
    void shouldPreserveFieldsInRoundTrip() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User originalDomain = User.builder()
                .id(99L)
                .username("testuser")
                .password("$2a$10$testHash")
                .email("test@example.com")
                .role(Role.ROLE_ANALYST)
                .enabled(true)
                .createdAt(now)
                .build();

        // When - Convert to entity and back to domain
        UserEntity entity = mapper.toEntity(originalDomain);
        User resultDomain = mapper.toDomain(entity);

        // Then
        assertThat(resultDomain).isNotNull();
        assertThat(resultDomain.getId()).isEqualTo(originalDomain.getId());
        assertThat(resultDomain.getUsername()).isEqualTo(originalDomain.getUsername());
        assertThat(resultDomain.getPassword()).isEqualTo(originalDomain.getPassword());
        assertThat(resultDomain.getEmail()).isEqualTo(originalDomain.getEmail());
        assertThat(resultDomain.getRole()).isEqualTo(originalDomain.getRole());
        assertThat(resultDomain.isEnabled()).isEqualTo(originalDomain.isEnabled());
    }

    @Test
    @DisplayName("Should handle null entity")
    void shouldHandleNullEntity() {
        // Given
        UserEntity entity = null;

        // When
        User domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle null domain")
    void shouldHandleNullDomain() {
        // Given
        User domain = null;

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should correctly map all three roles bidirectionally")
    void shouldMapAllRolesBidirectionally() {
        // Test ANALYST
        assertRoleMapping(Role.ROLE_ANALYST, UserEntity.RoleEntity.ROLE_ANALYST);

        // Test AFFILIATE
        assertRoleMapping(Role.ROLE_AFFILIATE, UserEntity.RoleEntity.ROLE_AFFILIATE);

        // Test ADMIN
        assertRoleMapping(Role.ROLE_ADMIN, UserEntity.RoleEntity.ROLE_ADMIN);
    }

    private void assertRoleMapping(Role domainRole, UserEntity.RoleEntity entityRole) {
        // Domain to Entity
        User domain = User.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .role(domainRole)
                .enabled(true)
                .build();

        UserEntity entity = mapper.toEntity(domain);
        assertThat(entity.getRole()).isEqualTo(entityRole);

        // Entity to Domain
        UserEntity testEntity = new UserEntity();
        testEntity.setUsername("test");
        testEntity.setDocument("12345678");
        testEntity.setPassword("test");
        testEntity.setFullName("Test");
        testEntity.setEmail("test@test.com");
        testEntity.setRole(entityRole);
        testEntity.setActive(true);

        User resultDomain = mapper.toDomain(testEntity);
        assertThat(resultDomain.getRole()).isEqualTo(domainRole);
    }
}
