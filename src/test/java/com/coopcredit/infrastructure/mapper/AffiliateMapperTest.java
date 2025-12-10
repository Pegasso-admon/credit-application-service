package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.enums.AffiliateStatus;
import com.coopcredit.infrastructure.controller.dto.AffiliateRequest;
import com.coopcredit.infrastructure.persistence.entity.AffiliateEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AffiliateMapper.
 * Tests all conversion methods: entity ↔ domain, request → domain, and list
 * mappings.
 */
@DisplayName("AffiliateMapper Tests")
class AffiliateMapperTest {

    private final AffiliateMapper mapper = new AffiliateMapperImpl();

    @Test
    @DisplayName("Should convert AffiliateEntity to Domain model")
    void shouldConvertEntityToDomain() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        AffiliateEntity entity = new AffiliateEntity();
        entity.setId(1L);
        entity.setDocument("12345678");
        entity.setFullName("Juan Perez");
        entity.setSalary(new BigDecimal("5000000.00"));
        entity.setAffiliationDate(LocalDate.of(2020, 1, 15));
        entity.setStatus(AffiliateEntity.AffiliateStatusEntity.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // When
        Affiliate domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getDocument()).isEqualTo("12345678");
        assertThat(domain.getName()).isEqualTo("Juan Perez");
        assertThat(domain.getSalary()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(domain.getAffiliationDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(domain.getStatus()).isEqualTo(AffiliateStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should convert Domain model to AffiliateEntity")
    void shouldConvertDomainToEntity() {
        // Given
        Affiliate domain = Affiliate.builder()
                .id(1L)
                .document("12345678")
                .name("Juan Perez")
                .salary(new BigDecimal("5000000.00"))
                .affiliationDate(LocalDate.of(2020, 1, 15))
                .status(AffiliateStatus.ACTIVE)
                .build();

        // When
        AffiliateEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getDocument()).isEqualTo("12345678");
        assertThat(entity.getFullName()).isEqualTo("Juan Perez");
        assertThat(entity.getSalary()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(entity.getAffiliationDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(entity.getStatus()).isEqualTo(AffiliateEntity.AffiliateStatusEntity.ACTIVE);
    }

    @Test
    @DisplayName("Should map INACTIVE status correctly")
    void shouldMapInactiveStatus() {
        // Given
        AffiliateEntity entity = new AffiliateEntity();
        entity.setStatus(AffiliateEntity.AffiliateStatusEntity.INACTIVE);
        entity.setDocument("12345678");
        entity.setFullName("Test");
        entity.setSalary(BigDecimal.valueOf(1000000));
        entity.setAffiliationDate(LocalDate.now());

        // When
        Affiliate domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getStatus()).isEqualTo(AffiliateStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should convert list of entities to list of domains")
    void shouldConvertEntityListToDomainList() {
        // Given
        AffiliateEntity entity1 = createTestEntity(1L, "12345678", "Juan Perez");
        AffiliateEntity entity2 = createTestEntity(2L, "87654321", "Maria Garcia");
        List<AffiliateEntity> entities = List.of(entity1, entity2);

        // When
        List<Affiliate> domains = mapper.toDomainList(entities);

        // Then
        assertThat(domains).hasSize(2);
        assertThat(domains.get(0).getId()).isEqualTo(1L);
        assertThat(domains.get(0).getDocument()).isEqualTo("12345678");
        assertThat(domains.get(1).getId()).isEqualTo(2L);
        assertThat(domains.get(1).getDocument()).isEqualTo("87654321");
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        // Given
        AffiliateEntity entity = null;

        // When
        Affiliate domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle empty list")
    void shouldHandleEmptyList() {
        // Given
        List<AffiliateEntity> emptyList = List.of();

        // When
        List<Affiliate> domains = mapper.toDomainList(emptyList);

        // Then
        assertThat(domains).isEmpty();
    }

    @Test
    @DisplayName("Should preserve all fields during round-trip conversion")
    void shouldPreserveFieldsInRoundTrip() {
        // Given
        Affiliate originalDomain = Affiliate.builder()
                .id(99L)
                .document("99999999")
                .name("Test User")
                .salary(new BigDecimal("7500000.00"))
                .affiliationDate(LocalDate.of(2019, 6, 1))
                .status(AffiliateStatus.INACTIVE)
                .build();

        // When - Convert to entity and back to domain
        AffiliateEntity entity = mapper.toEntity(originalDomain);
        Affiliate resultDomain = mapper.toDomain(entity);

        // Then
        assertThat(resultDomain).isNotNull();
        assertThat(resultDomain.getId()).isEqualTo(originalDomain.getId());
        assertThat(resultDomain.getDocument()).isEqualTo(originalDomain.getDocument());
        assertThat(resultDomain.getName()).isEqualTo(originalDomain.getName());
        assertThat(resultDomain.getSalary()).isEqualByComparingTo(originalDomain.getSalary());
        assertThat(resultDomain.getAffiliationDate()).isEqualTo(originalDomain.getAffiliationDate());
        assertThat(resultDomain.getStatus()).isEqualTo(originalDomain.getStatus());
    }

    // Helper method to create test entities
    private AffiliateEntity createTestEntity(Long id, String document, String fullName) {
        AffiliateEntity entity = new AffiliateEntity();
        entity.setId(id);
        entity.setDocument(document);
        entity.setFullName(fullName);
        entity.setSalary(new BigDecimal("5000000.00"));
        entity.setAffiliationDate(LocalDate.now());
        entity.setStatus(AffiliateEntity.AffiliateStatusEntity.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
