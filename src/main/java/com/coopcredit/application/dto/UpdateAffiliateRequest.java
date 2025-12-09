package com.coopcredit.application.dto;

import com.coopcredit.domain.model.enums.AffiliateStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * UpdateAffiliateRequest DTO.
 *
 * @param name   new name (optional)
 * @param salary new salary (optional, must be positive if provided)
 * @param status new status (optional)
 */
public record UpdateAffiliateRequest(
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,

        @DecimalMin(value = "0.01", message = "Salary must be greater than zero") BigDecimal salary,

        AffiliateStatus status) {
}
