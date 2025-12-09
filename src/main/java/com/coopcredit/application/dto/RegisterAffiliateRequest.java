package com.coopcredit.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RegisterAffiliateRequest DTO.
 * <p>
 * Data transfer object for affiliate registration requests.
 * Includes validation annotations for API layer.
 * </p>
 *
 * @param document        unique identification document
 * @param name            full name of the affiliate
 * @param salary          monthly salary (must be positive)
 * @param affiliationDate optional date of affiliation
 */
public record RegisterAffiliateRequest(
        @NotBlank(message = "Document is required") @Size(min = 5, max = 20, message = "Document must be between 5 and 20 characters") String document,

        @NotBlank(message = "Name is required") @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,

        @NotNull(message = "Salary is required") @DecimalMin(value = "0.01", message = "Salary must be greater than zero") BigDecimal salary,

        @PastOrPresent(message = "Affiliation date cannot be in the future") LocalDate affiliationDate) {
}
