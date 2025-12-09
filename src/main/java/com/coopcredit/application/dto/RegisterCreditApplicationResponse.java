package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RegisterCreditApplicationResponse DTO.
 *
 * @param id                generated application ID
 * @param affiliateDocument document of the applicant
 * @param affiliateName     name of the applicant
 * @param requestedAmount   amount requested
 * @param termMonths        repayment term
 * @param interestRate      annual interest rate
 * @param monthlyPayment    calculated monthly payment
 * @param applicationDate   when application was created
 * @param status            current application status
 */
public record RegisterCreditApplicationResponse(
        Long id,
        String affiliateDocument,
        String affiliateName,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal interestRate,
        BigDecimal monthlyPayment,
        LocalDateTime applicationDate,
        ApplicationStatus status) {
}
