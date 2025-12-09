package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.AffiliateStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RegisterAffiliateResponse DTO.
 *
 * @param id                     generated affiliate ID
 * @param document               affiliate's document
 * @param name                   affiliate's name
 * @param salary                 monthly salary
 * @param affiliationDate        date of affiliation
 * @param status                 affiliate status
 * @param monthsSinceAffiliation calculated seniority
 * @param canApplyForCredit      eligibility flag
 */
public record RegisterAffiliateResponse(
        Long id,
        String document,
        String name,
        BigDecimal salary,
        LocalDate affiliationDate,
        AffiliateStatus status,
        Long monthsSinceAffiliation,
        Boolean canApplyForCredit) {
}
