package com.parivahan.backend.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChallanDto {
    private Long id;
    private String offence;
    private BigDecimal amount;
    private LocalDate challanDate;
}
