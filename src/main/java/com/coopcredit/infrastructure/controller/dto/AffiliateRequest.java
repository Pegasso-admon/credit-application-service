package com.coopcredit.infrastructure.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AffiliateRequest(
        @NotBlank(message = "Document is required") @Size(min = 5, max = 20, message = "Document must be between 5 and 20 characters") String document,

        @NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,

        @NotNull(message = "Salary is required") @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than zero") BigDecimal salary,

        @NotNull(message = "Affiliation date is required") @PastOrPresent(message = "Affiliation date cannot be in the future") LocalDate affiliationDate) {
}
