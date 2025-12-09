package com.coopcredit.domain.service;

import java.math.BigDecimal;

/**
 * Service port for external risk evaluation operations.
 * <p>
 * This port defines the contract for interacting with external credit
 * risk bureaus or risk assessment services. It abstracts the communication
 * with the risk-central-mock-service microservice.
 * </p>
 *
 * <h2>Hexagonal Architecture:</h2>
 * <p>
 * This is an OUTPUT port (driven/secondary) that will be implemented
 * by infrastructure adapters using REST clients, message queues, or
 * other integration mechanisms.
 * </p>
 *
 * <h2>Implementation Requirements:</h2>
 * <ul>
 * <li>Must ensure consistent results for same document</li>
 * <li>Handle network failures gracefully</li>
 * <li>Implement timeout and retry strategies</li>
 * <li>Log all external service interactions</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 * <li>Same document must always return same score (idempotency)</li>
 * <li>Score range: 300-950</li>
 * <li>Risk levels: LOW (701-950), MEDIUM (501-700), HIGH (300-500)</li>
 * </ul>
 */
public interface RiskEvaluationPort {

    /**
     * Evaluates credit risk for an affiliate's credit application.
     * <p>
     * This method communicates with the external risk-central service
     * to obtain a credit score and risk level assessment.
     * </p>
     *
     * <h3>Expected Behavior:</h3>
     * <ul>
     * <li>Results must be deterministic per document number</li>
     * <li>Same document always returns same score</li>
     * <li>Different documents return different scores</li>
     * </ul>
     *
     * @param document        the affiliate's identification document
     * @param requestedAmount the credit amount being requested
     * @param termMonths      the requested term in months
     * @return RiskEvaluationResponse containing score, risk level, and details
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException         if external service is unavailable
     */
    RiskEvaluationResponse evaluateRisk(String document, BigDecimal requestedAmount, Integer termMonths);

    /**
     * Data transfer object for risk evaluation responses.
     * <p>
     * This nested class is part of the port contract and should remain
     * framework-agnostic.
     * </p>
     */
    record RiskEvaluationResponse(
            String document,
            Integer score,
            String riskLevel,
            String detail) {
        /**
         * Creates a new RiskEvaluationResponse.
         *
         * @param document  affiliate's document number
         * @param score     credit score (300-950)
         * @param riskLevel risk classification (LOW, MEDIUM, HIGH)
         * @param detail    additional evaluation details
         * @throws IllegalArgumentException if score is out of range
         */
        public RiskEvaluationResponse {
            if (score < 300 || score > 950) {
                throw new IllegalArgumentException("Score must be between 300 and 950");
            }
            if (document == null || document.isBlank()) {
                throw new IllegalArgumentException("Document cannot be null or empty");
            }
            if (riskLevel == null || riskLevel.isBlank()) {
                throw new IllegalArgumentException("Risk level cannot be null or empty");
            }
        }
    }
}