package com.coopcredit.riskcentralmock.dto;

/**
 * Response DTO for risk evaluation.
 *
 * @param documento    the affiliate's document number
 * @param score        the credit score (300-950)
 * @param nivelRiesgo  the risk level (BAJO, MEDIO, ALTO)
 * @param detalle      detailed description of the evaluation
 */
public record RiskEvaluationResponse(
        String documento,
        Integer score,
        String nivelRiesgo,
        String detalle
) {
}