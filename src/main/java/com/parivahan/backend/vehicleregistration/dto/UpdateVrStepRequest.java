package com.parivahan.backend.vehicleregistration.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateVrStepRequest {
    private Integer completedStep;

    // Step 1
    private String state;
    private String stateCode;

    // Step 2
    private String rtoCode;
    private String rtoName;

    // Step 3
    private String vehicleCategory;
    private String usageType;
    private String vehicleType;
    private Boolean isEligible;

    // Step 4
    private String identityProof;
    private String addressProof;
    private String vehicleInvoice;
    private String insuranceProof;
    private Boolean documentsConfirmed;

    // Step 5
    private BigDecimal feeAmount;

    // Step 6
    private LocalDate appointmentDate;
    private String appointmentSlot;
}
