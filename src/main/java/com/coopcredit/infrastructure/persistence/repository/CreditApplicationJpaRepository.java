package com.coopcredit.infrastructure.persistence.repository;

import com.coopcredit.infrastructure.persistence.entity.CreditApplicationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditApplicationJpaRepository extends JpaRepository<CreditApplicationEntity, Long> {

    @EntityGraph(attributePaths = "affiliate")
    @Query("SELECT ca FROM CreditApplicationEntity ca WHERE ca.id = :id")
    Optional<CreditApplicationEntity> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = "affiliate")
    List<CreditApplicationEntity> findByAffiliateId(Long affiliateId);

    @EntityGraph(attributePaths = "affiliate")
    List<CreditApplicationEntity> findByStatus(CreditApplicationEntity.ApplicationStatusEntity status);

    @Override
    @EntityGraph(attributePaths = "affiliate")
    List<CreditApplicationEntity> findAll();
}
