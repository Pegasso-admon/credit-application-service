package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CreditApplicationResponse DTO.
 * <p>
 * Generic response for credit application queries.
 * </p>
 *
 * @param id                application ID
 * @param affiliateId       affiliate's ID
 * @param affiliateDocument affiliate's document
 * @param affiliateName     affiliate's name
 * @param requestedAmount   amount requested
 * @param termMonths        repayment term
 * @param interestRate      annual interest rate
 * @param monthlyPayment    calculated monthly payment
 * @param applicationDate   creation timestamp
 * @param status            current status
 * @param riskScore         risk evaluation score (if evaluated)
 * @param riskLevel         risk classification (if evaluated)
 * @param decisionReason    approval/rejection reason (if evaluated)
 */
public record CreditApplicationResponse(
        Long id,
        Long affiliateId,
        String affiliateDocument,
        String affiliateName,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal interestRate,
        BigDecimal monthlyPayment,
        LocalDateTime applicationDate,
        ApplicationStatus status,
        Integer riskScore,
        String riskLevel,
        String decisionReason) {
}
