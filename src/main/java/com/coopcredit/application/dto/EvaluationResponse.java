package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EvaluationResponse DTO.
 * <p>
 * Complete response after credit application evaluation.
 * </p>
 *
 * @param applicationId        evaluated application ID
 * @param affiliateDocument    applicant's document
 * @param affiliateName        applicant's name
 * @param requestedAmount      amount requested
 * @param termMonths           repayment term
 * @param monthlyPayment       calculated monthly payment
 * @param status               final application status
 * @param approved             whether application was approved
 * @param decisionReason       reason for approval/rejection
 * @param riskScore            credit bureau score
 * @param riskLevel            risk classification
 * @param riskDetail           risk evaluation details
 * @param paymentToIncomeRatio calculated ratio
 * @param evaluatedAt          evaluation timestamp
 */
public record EvaluationResponse(
        Long applicationId,
        String affiliateDocument,
        String affiliateName,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal monthlyPayment,
        ApplicationStatus status,
        Boolean approved,
        String decisionReason,
        Integer riskScore,
        String riskLevel,
        String riskDetail,
        BigDecimal paymentToIncomeRatio,
        LocalDateTime evaluatedAt) {
}
