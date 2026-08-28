package com.parivahan.backend.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ComplianceItemDto {

    public enum ComplianceStatus {
        VALID, EXPIRING_SOON, EXPIRED, NOT_APPLICABLE
    }

    private ComplianceStatus status;
    private LocalDate validTill;  // null when NOT_APPLICABLE
}
