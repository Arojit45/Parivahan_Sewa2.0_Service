package com.parivahan.backend.fleet.dto;

import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FleetRouteDto {
    private Long id;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String startLocation;
    private String destination;
    private FleetRouteStatus routeStatus;
    private Integer toleranceMeters;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    // Live location of vehicle on this route
    private Double currentLat;
    private Double currentLng;
    private String currentAddress;
    private Double speed;
}
