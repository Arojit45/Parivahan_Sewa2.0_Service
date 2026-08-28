package com.parivahan.backend.challan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentReceiptDto {
    private String receiptNumber;
    private String registrationNumber;
    private String offence;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String transactionId;
    private String paymentMode;
    private String message;
}
