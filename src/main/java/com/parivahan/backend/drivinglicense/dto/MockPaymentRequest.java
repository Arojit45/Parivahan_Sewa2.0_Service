package com.parivahan.backend.drivinglicense.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MockPaymentRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Simulated payment outcome from the frontend.
     * Acceptable values: "SUCCESS" or "FAILED"
     */
    @NotBlank(message = "Simulated result is required")
    @Pattern(regexp = "^(SUCCESS|FAILED)$", message = "simulatedResult must be SUCCESS or FAILED")
    private String simulatedResult;
}
