package com.coopcredit.infrastructure.client.dto;

public record RiskEvaluationResponse(
        String document,
        Integer score,
        String riskLevel,
        String detail) {
}
