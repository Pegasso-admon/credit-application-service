package com.coopcredit.infrastructure.persistence.adapter;

import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.enums.ApplicationStatus;
import com.coopcredit.domain.repository.CreditApplicationRepositoryPort;
import com.coopcredit.infrastructure.persistence.entity.CreditApplicationEntity;
import com.coopcredit.infrastructure.mapper.CreditApplicationMapper;
import com.coopcredit.infrastructure.persistence.repository.CreditApplicationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the CreditApplicationRepositoryPort.
 * This adapter translates between domain models and JPA entities,
 * managing credit application persistence with optimized queries.
 *
 * @see CreditApplicationRepositoryPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class JpaCreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {

    private final CreditApplicationJpaRepository jpaRepository;
    private final CreditApplicationMapper mapper;

    @Override
    public CreditApplication save(CreditApplication application) {
        validateApplication(application);

        log.debug("Saving credit application for affiliate ID: {}",
                application.getAffiliate().getId());

        CreditApplicationEntity entity = mapper.toEntity(application);
        CreditApplicationEntity savedEntity = jpaRepository.save(entity);
        CreditApplication savedApplication = mapper.toDomain(savedEntity);

        log.info("Successfully saved credit application with ID: {}", savedEntity.getId());

        return savedApplication;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditApplication> findById(Long id) {
        validateId(id);

        log.debug("Finding credit application by ID: {}", id);

        return jpaRepository.findByIdWithDetails(id)
                .map(entity -> {
                    log.debug("Credit application found with ID: {}", id);
                    return mapper.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditApplication> findByAffiliateId(Long affiliateId) {
        validateId(affiliateId);

        log.debug("Finding credit applications for affiliate ID: {}", affiliateId);

        List<CreditApplicationEntity> entities = jpaRepository.findByAffiliateId(affiliateId);
        List<CreditApplication> applications = mapper.toDomainList(entities);

        log.debug("Found {} credit applications for affiliate ID: {}",
                applications.size(), affiliateId);

        return applications;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditApplication> findByStatus(ApplicationStatus status) {
        validateStatus(status);

        log.debug("Finding credit applications with status: {}", status);

        CreditApplicationEntity.ApplicationStatusEntity entityStatus = mapStatusToEntity(status);

        List<CreditApplicationEntity> entities = jpaRepository.findByStatus(entityStatus);
        List<CreditApplication> applications = mapper.toDomainList(entities);

        log.debug("Found {} credit applications with status: {}",
                applications.size(), status);

        return applications;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditApplication> findAll() {
        log.debug("Finding all credit applications");

        List<CreditApplicationEntity> entities = jpaRepository.findAll();
        List<CreditApplication> applications = mapper.toDomainList(entities);

        log.debug("Found {} total credit applications", applications.size());

        return applications;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        log.debug("Deleting credit application with ID: {}", id);

        if (!jpaRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent credit application with ID: {}", id);
            throw new IllegalArgumentException("Credit application with ID " + id +
                    " does not exist");
        }

        jpaRepository.deleteById(id);
        log.info("Successfully deleted credit application with ID: {}", id);
    }

    private void validateApplication(CreditApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("Credit application cannot be null");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }

    private void validateStatus(ApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Application status cannot be null");
        }
    }

    private CreditApplicationEntity.ApplicationStatusEntity mapStatusToEntity(
            ApplicationStatus status) {
        return switch (status) {
            case PENDING -> CreditApplicationEntity.ApplicationStatusEntity.PENDING;
            case APPROVED -> CreditApplicationEntity.ApplicationStatusEntity.APPROVED;
            case REJECTED -> CreditApplicationEntity.ApplicationStatusEntity.REJECTED;
            case CANCELLED -> CreditApplicationEntity.ApplicationStatusEntity.CANCELLED;
        };
    }
}