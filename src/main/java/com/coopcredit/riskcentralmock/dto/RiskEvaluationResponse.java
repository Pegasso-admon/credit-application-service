package com.coopcredit.riskcentralmock.dto;

/**
 * Response DTO for risk evaluation.
 *
 * @param document  the affiliate's document number
 * @param score     the credit score (300-950)
 * @param riskLevel the risk level (LOW, MEDIUM, HIGH)
 * @param detail    detailed description of the evaluation
 */
public record RiskEvaluationResponse(
                String document,
                Integer score,
                String riskLevel,
                String detail) {
}