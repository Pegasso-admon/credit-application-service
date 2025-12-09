package com.coopcredit.application.usecase;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.model.enums.AffiliateStatus;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.model.enums.RiskLevel;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;
import com.coopcredit.domain.service.RiskEvaluationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluateCreditApplicationUseCaseTest {

    @Mock
    private CreditApplicationRepositoryPort creditApplicationRepository;

    @Mock
    private RiskEvaluationPort riskEvaluationPort;

    @InjectMocks
    private EvaluateCreditApplicationUseCase evaluateUseCase;

    private CreditApplication pendingApplication;
    private Affiliate activeAffiliate;

    @BeforeEach
    void setUp() {
        activeAffiliate = Affiliate.builder()
                .id(1L)
                .document("123456789")
                .name("John Doe")
                .salary(new BigDecimal("5000.00"))
                .affiliationDate(LocalDate.now().minusMonths(12))
                .status(AffiliateStatus.ACTIVE)
                .build();

        pendingApplication = CreditApplication.builder()
                .id(1L)
                .affiliate(activeAffiliate)
                .requestedAmount(new BigDecimal("10000.00"))
                .termMonths(12)
                .interestRate(new BigDecimal("10.00"))
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.PENDING)
                .build();
    }

    @Test
    void shouldApproveApplicationWhenLowRiskAndPoliciesMet() {
        // Given
        RiskEvaluation lowRisk = RiskEvaluation.builder()
                .score(800)
                .riskLevel(RiskLevel.LOW)
                .approved(true)
                .build();

        when(creditApplicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(riskEvaluationPort.evaluateRisk(any(String.class), any(BigDecimal.class), any(Integer.class)))
                .thenReturn(lowRisk);
        when(creditApplicationRepository.save(any(CreditApplication.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        var result = evaluateUseCase.execute(1L);

        // Then
        assertThat(result.approved()).isTrue();
        assertThat(result.application().getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(riskEvaluationPort).evaluateRisk(eq(activeAffiliate.getDocument()), any(BigDecimal.class),
                any(Integer.class));
    }

    @Test
    void shouldRejectApplicationWhenHighRisk() {
        // Given
        RiskEvaluation highRisk = RiskEvaluation.builder()
                .score(400)
                .riskLevel(RiskLevel.HIGH)
                .approved(false)
                .rejectionReason("High Risk")
                .build();

        when(creditApplicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(riskEvaluationPort.evaluateRisk(any(String.class), any(BigDecimal.class), any(Integer.class)))
                .thenReturn(highRisk);
        when(creditApplicationRepository.save(any(CreditApplication.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        var result = evaluateUseCase.execute(1L);

        // Then
        assertThat(result.approved()).isFalse();
        assertThat(result.application().getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(result.reason()).contains("High risk");
    }

    @Test
    void shouldThrowExceptionWhenApplicationNotFound() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluateUseCase.execute(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
