package com.parivahan.backend.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplianceStatusDto {
    private ComplianceItemDto rc;
    private ComplianceItemDto puc;
    private ComplianceItemDto insurance;
    private ComplianceItemDto tax;
    private ComplianceItemDto permit;
    private ComplianceItemDto fitness;
}
