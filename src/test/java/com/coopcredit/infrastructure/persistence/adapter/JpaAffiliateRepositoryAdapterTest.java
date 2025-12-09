package com.coopcredit.infrastructure.persistence.adapter;

import com.coopcredit.AbstractIntegrationTest;
import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.enums.AffiliateStatus;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JpaAffiliateRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private AffiliateRepositoryPort affiliateRepository;

    @Test
    void shouldSaveAndFindAffiliate() {
        // Given
        Affiliate affiliate = Affiliate.builder()
                .document("123456789")
                .name("John Doe")
                .salary(new BigDecimal("5000.00"))
                .affiliationDate(LocalDate.now().minusMonths(7))
                .status(AffiliateStatus.ACTIVE)
                .build();

        // When
        Affiliate saved = affiliateRepository.save(affiliate);

        // Then
        assertThat(saved.getId()).isNotNull();

        Optional<Affiliate> found = affiliateRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDocument()).isEqualTo("123456789");
    }
}
