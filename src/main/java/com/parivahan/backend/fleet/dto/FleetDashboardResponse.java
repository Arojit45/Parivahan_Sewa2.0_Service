package com.parivahan.backend.fleet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FleetDashboardResponse {
    // Fleet info
    private Long fleetId;
    private String fleetName;
    private String fleetRegistrationNumber;

    // Summary stats
    private int totalVehicles;
    private int onlineVehicles;
    private int offlineVehicles;
    private int activeRoutes;
    private int openAlerts;

    // Vehicles with live GPS + status
    private List<FleetVehicleDto> vehicles;

    // Active routes
    private List<FleetRouteDto> routes;

    // Fleet alerts
    private List<FleetAlertDto> alerts;
}
