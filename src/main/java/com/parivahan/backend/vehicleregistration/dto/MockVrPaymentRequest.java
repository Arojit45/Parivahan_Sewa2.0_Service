package com.parivahan.backend.vehicleregistration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MockVrPaymentRequest {
    @NotNull(message = "Application ID is required")
    private Long applicationId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Simulated result is required")
    private String simulatedResult; // SUCCESS, FAILURE
}
