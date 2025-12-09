package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.AffiliateStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AffiliateResponse DTO.
 * <p>
 * Response for affiliate queries.
 * </p>
 *
 * @param id                     affiliate ID
 * @param document               identification document
 * @param name                   full name
 * @param salary                 monthly salary
 * @param affiliationDate        date of affiliation
 * @param status                 current status
 * @param monthsSinceAffiliation seniority in months
 * @param canApplyForCredit      eligibility flag
 */
public record AffiliateResponse(
        Long id,
        String document,
        String name,
        BigDecimal salary,
        LocalDate affiliationDate,
        AffiliateStatus status,
        Long monthsSinceAffiliation,
        Boolean canApplyForCredit) {
}
