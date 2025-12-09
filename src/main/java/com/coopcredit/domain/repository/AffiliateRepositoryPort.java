package com.coopcredit.domain.repository;

import com.coopcredit.domain.model.Affiliate;

import java.util.Optional;

/**
 * Repository port for Affiliate aggregate persistence operations.
 * <p>
 * This port defines the contract for affiliate data access without
 * coupling the domain to any specific persistence technology.
 * Implementations (adapters) will provide concrete persistence mechanisms
 * such as JPA, MongoDB, or in-memory storage.
 * </p>
 *
 * <h2>Hexagonal Architecture:</h2>
 * <p>
 * This is an OUTPUT port (driven/secondary) that will be implemented
 * by infrastructure adapters. The domain layer depends on this abstraction,
 * not on concrete implementations.
 * </p>
 *
 * <h2>Implementation Requirements:</h2>
 * <ul>
 *   <li>Document uniqueness must be enforced</li>
 *   <li>All operations should be transactional</li>
 *   <li>Concurrent access should be handled safely</li>
 * </ul>
 */
public interface AffiliateRepositoryPort {
    
    /**
     * Persists a new affiliate or updates an existing one.
     *
     * @param affiliate the affiliate to save
     * @return the persisted affiliate with generated ID if new
     * @throws IllegalArgumentException if affiliate is null
     * @throws RuntimeException if document already exists for a different affiliate
     */
    Affiliate save(Affiliate affiliate);
    
    /**
     * Retrieves an affiliate by their unique identifier.
     *
     * @param id the affiliate's unique identifier
     * @return Optional containing the affiliate if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Affiliate> findById(Long id);
    
    /**
     * Retrieves an affiliate by their document number.
     * <p>
     * Document number is a business key and must be unique.
     * </p>
     *
     * @param document the affiliate's identification document
     * @return Optional containing the affiliate if found, empty otherwise
     * @throws IllegalArgumentException if document is null or blank
     */
    Optional<Affiliate> findByDocument(String document);
    
    /**
     * Checks if an affiliate with the given document exists.
     *
     * @param document the document number to check
     * @return true if an affiliate with this document exists
     * @throws IllegalArgumentException if document is null or blank
     */
    boolean existsByDocument(String document);
    
    /**
     * Deletes an affiliate by their unique identifier.
     * <p>
     * Note: Physical deletion should be used cautiously in production.
     * Consider soft deletion (status change) for audit purposes.
     * </p>
     *
     * @param id the affiliate's unique identifier
     * @throws IllegalArgumentException if id is null
     */
    void deleteById(Long id);
}