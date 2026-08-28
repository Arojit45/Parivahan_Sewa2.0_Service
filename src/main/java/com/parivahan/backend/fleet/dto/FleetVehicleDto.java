package com.parivahan.backend.fleet.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FleetVehicleDto {
    private Long id;
    private Long vehicleId;
    private String registrationNumber;
    private String nickname;
    private String manufacturer;
    private String model;
    private String fuelType;
    // Live GPS data
    private Double latitude;
    private Double longitude;
    private Double speed;
    private String heading;
    private String address;
    private LocalDateTime lastUpdated;
    // Derived status
    private String onlineStatus; // ONLINE, OFFLINE, AT_RISK
    private String routeInfo;    // "Kolkata → Durgapur" or null
    private boolean hasAlert;
}
