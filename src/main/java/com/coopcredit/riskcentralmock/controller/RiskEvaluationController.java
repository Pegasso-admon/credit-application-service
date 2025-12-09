package com.coopcredit.riskcentralmock.controller;

import com.coopcredit.riskcentralmock.dto.RiskEvaluationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * Controller for the Risk Central Mock Service.
 * Simulates risk evaluation logic based on document hash.
 */
@RestController
@RequestMapping("/risk-central/api/v1/evaluations")
@Slf4j
public class RiskEvaluationController {

    private final Random random = new Random();

    /**
     * Evaluates risk for a given document.
     * The logic is deterministic based on the document number to ensure
     * reproducibility.
     *
     * @param document the document number
     * @return the risk evaluation response
     */
    @GetMapping("/{document}")
    public ResponseEntity<RiskEvaluationResponse> evaluateRisk(@PathVariable String document) {
        log.info("Received risk evaluation request for document: {}", document);

        // Deterministic logic based on document hash
        int hash = document.hashCode();
        int score = 300 + (Math.abs(hash) % 651); // Score between 300 and 950

        String riskLevel;
        String detail;

        if (score >= 750) {
            riskLevel = "LOW";
            detail = "Excellent credit history. Low probability of default.";
        } else if (score >= 600) {
            riskLevel = "MEDIUM";
            detail = "Average credit history. Standard terms apply.";
        } else {
            riskLevel = "HIGH";
            detail = "Poor credit history or insufficient data. High probability of default.";
        }

        RiskEvaluationResponse response = new RiskEvaluationResponse(
                document,
                score,
                riskLevel,
                detail);

        log.info("Evaluation complete for document {}: Score={}, Level={}", document, score, riskLevel);
        return ResponseEntity.ok(response);
    }
}
