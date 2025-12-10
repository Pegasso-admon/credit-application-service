package com.coopcredit.application.usecase;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.enums.AffiliateStatus;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import org.springframework.stereotype.Service;

/**
 * Use case for registering a new affiliate in the system.
 * <p>
 * This use case orchestrates the business logic for affiliate registration,
 * including validation of business rules and persistence through repository
 * ports.
 * </p>
 *
 * <h2>Preconditions:</h2>
 * <ul>
 * <li>Document number must be unique</li>
 * <li>Salary must be greater than zero</li>
 * <li>All required fields must be provided</li>
 * </ul>
 *
 * <h2>Postconditions:</h2>
 * <ul>
 * <li>Affiliate is persisted with ACTIVE status</li>
 * <li>Affiliation date is set to current date if not provided</li>
 * <li>Domain events are triggered (if event system is implemented)</li>
 * </ul>
 *
 * <h2>Business Rules Applied:</h2>
 * <ul>
 * <li>Document uniqueness validation</li>
 * <li>Salary positivity validation</li>
 * <li>Default status is ACTIVE</li>
 * </ul>
 */
@Service
public class RegisterAffiliateUseCase {

    private final AffiliateRepositoryPort affiliateRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param affiliateRepository repository port for affiliate persistence
     * @throws IllegalArgumentException if repository is null
     */
    public RegisterAffiliateUseCase(AffiliateRepositoryPort affiliateRepository) {
        if (affiliateRepository == null) {
            throw new IllegalArgumentException("AffiliateRepository cannot be null");
        }
        this.affiliateRepository = affiliateRepository;
    }

    /**
     * Executes the affiliate registration use case.
     * <p>
     * Flow:
     * <ol>
     * <li>Validate document uniqueness</li>
     * <li>Create affiliate domain object with validations</li>
     * <li>Persist affiliate through repository</li>
     * <li>Return persisted affiliate</li>
     * </ol>
     * </p>
     *
     * @param request the registration request containing affiliate data
     * @return the registered affiliate with generated ID
     * @throws IllegalArgumentException if document already exists
     * @throws IllegalArgumentException if request data is invalid
     */
    public Affiliate execute(RegisterAffiliateRequest request) {
        validateRequest(request);
        validateDocumentUniqueness(request.document());

        Affiliate affiliate = buildAffiliateFromRequest(request);

        return affiliateRepository.save(affiliate);
    }

    /**
     * Validates the request parameters.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if request is null or invalid
     */
    private void validateRequest(RegisterAffiliateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    }

    /**
     * Validates that the document number is unique.
     *
     * @param document the document to check
     * @throws IllegalArgumentException if document already exists
     */
    private void validateDocumentUniqueness(String document) {
        if (affiliateRepository.existsByDocument(document)) {
            throw new IllegalArgumentException(
                    String.format("Affiliate with document %s already exists", document));
        }
    }

    /**
     * Builds an Affiliate domain object from the request.
     *
     * @param request the registration request
     * @return constructed Affiliate object
     */
    private Affiliate buildAffiliateFromRequest(RegisterAffiliateRequest request) {
        return Affiliate.builder()
                .document(request.document())
                .name(request.name())
                .salary(request.salary())
                .affiliationDate(request.affiliationDate())
                .status(request.status() != null ? request.status() : AffiliateStatus.ACTIVE)
                .build();
    }

    /**
     * Request DTO for affiliate registration.
     *
     * @param document        unique identification document
     * @param name            full name of the affiliate
     * @param salary          monthly salary
     * @param affiliationDate date of affiliation (null for current date)
     * @param status          initial status (null for ACTIVE default)
     */
    public record RegisterAffiliateRequest(
            String document,
            String name,
            java.math.BigDecimal salary,
            java.time.LocalDate affiliationDate,
            AffiliateStatus status) {
    }
}