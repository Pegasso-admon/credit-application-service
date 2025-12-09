
package com.coopcredit.domain.repository;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.model.CreditApplication;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for CreditApplication aggregate persistence operations.
 * <p>
 * This port defines the contract for credit application data access without
 * coupling the domain to any specific persistence technology.
 * It supports queries needed for different user roles and business processes.
 * </p>
 *
 * <h2>Hexagonal Architecture:</h2>
 * <p>
 * This is an OUTPUT port (driven/secondary) that will be implemented
 * by infrastructure adapters using JPA or other persistence mechanisms.
 * </p>
 *
 * <h2>Performance Considerations:</h2>
 * <ul>
 * <li>Implement eager/lazy loading strategies appropriately</li>
 * <li>Use EntityGraph or fetch joins to avoid N+1 problems</li>
 * <li>Consider pagination for list operations in production</li>
 * </ul>
 *
 * @author CoopCredit Development Team
 * @version 1.0
 * @since 2024
 */
public interface CreditApplicationRepositoryPort {

    /**
     * Persists a new credit application or updates an existing one.
     *
     * @param application the credit application to save
     * @return the persisted application with generated ID if new
     * @throws IllegalArgumentException if application is null
     */
    CreditApplication save(CreditApplication application);

    /**
     * Retrieves a credit application by its unique identifier.
     * <p>
     * Should fetch associated entities (Affiliate, RiskEvaluation)
     * to avoid lazy loading issues.
     * </p>
     *
     * @param id the application's unique identifier
     * @return Optional containing the application if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<CreditApplication> findById(Long id);

    /**
     * Retrieves all credit applications for a specific affiliate.
     * <p>
     * Used by affiliates to view their own application history.
     * Should be ordered by application date descending.
     * </p>
     *
     * @param affiliateId the affiliate's unique identifier
     * @return list of applications, empty list if none found
     * @throws IllegalArgumentException if affiliateId is null
     */
    List<CreditApplication> findByAffiliateId(Long affiliateId);

    /**
     * Retrieves all credit applications with a specific status.
     * <p>
     * Used by analysts to find pending applications for evaluation.
     * Should include associated Affiliate data.
     * </p>
     *
     * @param status the application status to filter by
     * @return list of applications, empty list if none found
     * @throws IllegalArgumentException if status is null
     */
    List<CreditApplication> findByStatus(ApplicationStatus status);

    /**
     * Retrieves all credit applications in the system.
     * <p>
     * Used by administrators for reporting and monitoring.
     * Consider adding pagination in production implementations.
     * </p>
     *
     * @return list of all applications, empty list if none exist
     */
    List<CreditApplication> findAll();

    /**
     * Deletes a credit application by its unique identifier.
     * <p>
     * Note: Physical deletion should be avoided in production.
     * Consider soft deletion or status change for audit compliance.
     * </p>
     *
     * @param id the application's unique identifier
     * @throws IllegalArgumentException if id is null
     */
    void deleteById(Long id);
}