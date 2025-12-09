package com.coopcredit.infrastructure.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreditApplicationRequest(
        @NotNull(message = "Affiliate ID is required") Long affiliateId,

        @NotNull(message = "Requested amount is required") @DecimalMin(value = "1000.0", message = "Minimum amount is 1000") BigDecimal requestedAmount,

        @NotNull(message = "Term is required") @Min(value = 6, message = "Minimum term is 6 months") @Max(value = 60, message = "Maximum term is 60 months") Integer termMonths) {
}
