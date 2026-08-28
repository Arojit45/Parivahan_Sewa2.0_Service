package com.parivahan.backend.vehicleregistration.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class VrApplicationResponse {
    private Long id;
    private String applicationNumber;

    private String state;
    private String stateCode;
    private String rtoCode;
    private String rtoName;

    private String vehicleCategory;
    private String usageType;
    private String vehicleType;
    private Boolean isEligible;

    private String identityProof;
    private String addressProof;
    private String vehicleInvoice;
    private String insuranceProof;
    private Boolean documentsConfirmed;

    private BigDecimal feeAmount;
    private String paymentStatus;
    private String paymentTransactionId;
    private LocalDateTime paymentTimestamp;

    private LocalDate appointmentDate;
    private String appointmentSlot;

    private String applicationStatus;
    private String inspectionStatus;

    private Integer lastCompletedStep;
}
