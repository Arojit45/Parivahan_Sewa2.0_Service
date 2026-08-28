package com.parivahan.backend.fleet.dto;

import com.parivahan.backend.fleet.enums.FleetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FleetRegistrationResponse {
    private Long id;
    private String fleetName;
    private String fleetRegistrationNumber;
    private FleetStatus status;
    private String rejectionReason;
    private String vehicleRegistrationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
