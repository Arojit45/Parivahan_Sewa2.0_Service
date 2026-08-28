package com.parivahan.backend.fleet.dto;

import com.parivahan.backend.fleet.enums.FleetAlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FleetAlertDto {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private FleetAlertType alertType;
    private String message;
    private String status; // OPEN / RESOLVED
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
