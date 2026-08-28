package com.parivahan.backend.drivinglicense.dto;

import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import com.parivahan.backend.drivinglicense.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private String applicationNumber;

    // Step 1
    private String state;
    private String stateCode;

    // Step 2
    private String rtoCode;
    private String rtoName;

    // Step 3
    private String vehicleClass;

    // Step 4
    private Boolean hasLL;
    private String llNumber;

    // Step 5
    private String applicantName;
    private LocalDate dob;
    private String address;
    private String aadharNumber;
    private Boolean isEligible;

    // Step 6
    private Boolean documentsConfirmed;
    private Long selectedDrivingSchoolId;
    private String selectedDrivingSchoolName;

    // Step 7
    private LocalDate appointmentDate;
    private String appointmentSlot;

    // Step 8
    private BigDecimal feeAmount;
    private PaymentStatus paymentStatus;
    private String paymentTransactionId;
    private LocalDateTime paymentTimestamp;

    // Step 9 / Admin
    private ApplicationStatus applicationStatus;
    private String testResult;

    // Progress
    private Integer lastCompletedStep;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
