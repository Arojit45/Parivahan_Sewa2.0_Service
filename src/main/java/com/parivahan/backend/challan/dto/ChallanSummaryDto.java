package com.parivahan.backend.challan.dto;

import com.parivahan.backend.challan.enums.ChallanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChallanSummaryDto {
    private Long id;
    private String registrationNumber;
    private String vehicleModel;       // "Tata Motors Nexon"
    private String vehicleNickname;    // "My Nexon"
    private String offence;
    private String location;           // mocked per-challan
    private BigDecimal amount;
    private LocalDate challanDate;
    private LocalDate dueDate;         // challanDate + 30 days
    private LocalDate paymentDate;
    private String transactionId;
    private ChallanStatus status;
    private boolean hasActiveDispute;
}
