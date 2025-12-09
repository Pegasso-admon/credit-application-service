package com.coopcredit.infrastructure.client.dto;

public record RiskEvaluationResponse(
        String documento,
        Integer score,
        String nivelRiesgo,
        String detalle) {
}
