package com.parivahan.backend.challan.dto;

import com.parivahan.backend.challan.enums.ChallanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChallanDetailDto {
    private Long id;
    private String registrationNumber;
    private String vehicleModel;
    private String offence;
    private BigDecimal amount;
    private LocalDate challanDate;
    private ChallanStatus status;
    private LocalDate paymentDate;
    private String transactionId;
    private boolean hasActiveDispute;
}
