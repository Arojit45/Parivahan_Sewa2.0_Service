package com.parivahan.backend.dashboard.dto;

import com.parivahan.backend.livelocation.dto.VehicleTwinDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponseDto {
    private VehicleCardDto vehicleCard;
    private VehicleTwinDto vehicleTwin;
    private ComplianceStatusDto compliance;
    private List<AlertDto> alerts;
    private List<ChallanDto> pendingChallans;
    private int healthScore;
    private String healthLabel;
}
