package com.coopcredit.infrastructure.controller;

import com.coopcredit.application.usecase.EvaluateCreditApplicationUseCase;
import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;
import com.coopcredit.infrastructure.controller.dto.CreditApplicationRequest;
import com.coopcredit.infrastructure.mapper.CreditApplicationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for credit applications.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/credit-applications")
@RequiredArgsConstructor
@Tag(name = "Credit Applications", description = "Operations for credit applications")
public class CreditApplicationController {

    private final CreditApplicationRepositoryPort repository;
    private final AffiliateRepositoryPort affiliateRepository;
    private final EvaluateCreditApplicationUseCase evaluateUseCase;
    private final CreditApplicationMapper mapper;
        @PostMapping
        @PreAuthorize("hasAnyRole('AFFILIATE', 'ANALYST', 'ADMIN')")
        @Operation(summary = "Create Application", description = "Creates a new credit application")
        public ResponseEntity<CreditApplication> createApplication(
                        @Valid @RequestBody CreditApplicationRequest request) {

                log.info("Creating application request for affiliate: {}", request.affiliateId());

                // First, verify the affiliate exists in the database
                Affiliate affiliate = affiliateRepository.findById(request.affiliateId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Affiliate not found with ID: " + request.affiliateId()));

                log.info("Found affiliate: {} ({})", affiliate.getName(), affiliate.getDocument());

                // Build the application directly with all required fields
                // Note: We cannot use mapper.toDomain(request) because the domain validation
                // happens in the constructor and requires affiliate to be non-null
                CreditApplication application = CreditApplication.builder()
                                .applicationDate(LocalDateTime.now())
                                .status(ApplicationStatus.PENDING)
                                .affiliate(affiliate)
                                .requestedAmount(request.requestedAmount())
                                .termMonths(request.termMonths())
                                .interestRate(java.math.BigDecimal.valueOf(18.5)) // Default interest rate
                                .build();

                CreditApplication saved = repository.save(application);
                return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
        public ResponseEntity<CreditApplication> getApplication(@PathVariable Long id) {
                return repository.findById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/pending")
        @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
        public ResponseEntity<List<CreditApplication>> getPendingApplications() {
                return ResponseEntity.ok(repository.findByStatus(ApplicationStatus.PENDING));
        }
}