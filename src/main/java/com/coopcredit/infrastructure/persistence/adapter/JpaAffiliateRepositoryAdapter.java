package com.coopcredit.infrastructure.persistence.adapter;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.repository.AffiliateRepositoryPort;
import com.coopcredit.infrastructure.persistence.entity.AffiliateEntity;
import com.coopcredit.infrastructure.mapper.AffiliateMapper;
import com.coopcredit.infrastructure.persistence.repository.AffiliateJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * JPA implementation of the AffiliateRepositoryPort.
 * This adapter translates between domain models and JPA entities,
 * implementing the hexagonal architecture pattern.
 *
 * @see AffiliateRepositoryPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class JpaAffiliateRepositoryAdapter implements AffiliateRepositoryPort {

    private final AffiliateJpaRepository jpaRepository;
    private final AffiliateMapper mapper;

    @Override
    public Affiliate save(Affiliate affiliate) {
        validateAffiliate(affiliate);

        try {
            log.debug("Saving affiliate with document: {}", affiliate.getDocument());

            AffiliateEntity entity = mapper.toEntity(affiliate);
            AffiliateEntity savedEntity = jpaRepository.save(entity);
            Affiliate savedAffiliate = mapper.toDomain(savedEntity);

            log.info("Successfully saved affiliate with ID: {} and document: {}",
                    savedEntity.getId(), affiliate.getDocument());

            return savedAffiliate;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when saving affiliate with document: {}",
                    affiliate.getDocument(), e);
            throw new RuntimeException("Affiliate with document " + affiliate.getDocument() +
                    " already exists", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Affiliate> findById(Long id) {
        validateId(id);

        log.debug("Finding affiliate by ID: {}", id);

        return jpaRepository.findById(id)
                .map(entity -> {
                    log.debug("Affiliate found with ID: {}", id);
                    return mapper.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Affiliate> findByDocument(String document) {
        validateDocument(document);

        log.debug("Finding affiliate by document: {}", document);

        return jpaRepository.findByDocument(document)
                .map(entity -> {
                    log.debug("Affiliate found with document: {}", document);
                    return mapper.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDocument(String document) {
        validateDocument(document);

        log.debug("Checking if affiliate exists with document: {}", document);

        boolean exists = jpaRepository.existsByDocument(document);
        log.debug("Affiliate with document {} exists: {}", document, exists);

        return exists;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        log.debug("Deleting affiliate with ID: {}", id);

        if (!jpaRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent affiliate with ID: {}", id);
            throw new IllegalArgumentException("Affiliate with ID " + id + " does not exist");
        }

        jpaRepository.deleteById(id);
        log.info("Successfully deleted affiliate with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Affiliate> findAll() {
        log.debug("Finding all affiliates");
        return mapper.toDomainList(jpaRepository.findAll());
    }

    private void validateAffiliate(Affiliate affiliate) {
        if (affiliate == null) {
            throw new IllegalArgumentException("Affiliate cannot be null");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Affiliate ID cannot be null");
        }
    }

    private void validateDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("Affiliate document cannot be null or blank");
        }
    }
}