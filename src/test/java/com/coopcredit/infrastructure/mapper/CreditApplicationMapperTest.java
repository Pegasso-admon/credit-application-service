package com.coopcredit.infrastructure.mapper;

import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.infrastructure.controller.dto.CreditApplicationRequest;
import com.coopcredit.infrastructure.persistence.entity.CreditApplicationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreditApplicationMapperTest {

    private final CreditApplicationMapper mapper = Mappers.getMapper(CreditApplicationMapper.class);

    @Test
    void shouldMapDTOToDomain() {
        // Given
        CreditApplicationRequest request = new CreditApplicationRequest(
                1L,
                new BigDecimal("5000.00"),
                12);

        // When
        CreditApplication domain = mapper.toDomain(request);

        // Then
        assertThat(domain.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(domain.getRequestedAmount()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(domain.getTermMonths()).isEqualTo(12);
        // Validating ignored fields
        assertThat(domain.getInterestRate()).isNull();
        assertThat(domain.getAffiliate()).isNull();
    }

    @Test
    void shouldMapDomainToEntity() {
        // Given
        CreditApplication domain = CreditApplication.builder()
                .id(100L)
                .requestedAmount(new BigDecimal("1000.00"))
                .status(ApplicationStatus.APPROVED)
                .applicationDate(LocalDateTime.now())
                .build();

        // When
        CreditApplicationEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getStatus()).isEqualTo(CreditApplicationEntity.ApplicationStatusEntity.APPROVED);
        assertThat(entity.getRequestedAmount()).isEqualTo(new BigDecimal("1000.00"));
    }
}
