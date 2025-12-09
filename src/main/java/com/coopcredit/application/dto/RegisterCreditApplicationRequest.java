package com.coopcredit.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * RegisterCreditApplicationRequest DTO.
 *
 * @param affiliateId     ID of the affiliate applying
 * @param requestedAmount amount requested (must be positive)
 * @param termMonths      repayment term in months
 * @param interestRate    annual interest rate percentage
 */
public record RegisterCreditApplicationRequest(
        @NotNull(message = "Affiliate ID is required") Long affiliateId,

        @NotNull(message = "Requested amount is required") @DecimalMin(value = "1.00", message = "Requested amount must be at least 1.00") @DecimalMax(value = "1000000000.00", message = "Requested amount is too large") BigDecimal requestedAmount,

        @NotNull(message = "Term in months is required") @Min(value = 1, message = "Term must be at least 1 month") @Max(value = 360, message = "Term cannot exceed 360 months") Integer termMonths,

        @NotNull(message = "Interest rate is required") @DecimalMin(value = "0.00", message = "Interest rate cannot be negative") @DecimalMax(value = "100.00", message = "Interest rate cannot exceed 100%") BigDecimal interestRate) {
}
