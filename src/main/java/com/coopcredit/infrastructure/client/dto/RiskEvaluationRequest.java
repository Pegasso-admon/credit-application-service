package com.coopcredit.infrastructure.client.dto;

import java.math.BigDecimal;

public record RiskEvaluationRequest(
        String documento,
        BigDecimal montoSolicitado,
        Integer plazoMeses) {
}
