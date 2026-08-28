package com.parivahan.backend.dashboard.dto;

import com.parivahan.backend.vehicle.enums.VehicleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleCardDto {
    private Long id;
    private String nickname;
    private String registrationNumber;
    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String rto;
    private String owner;
    private String registrationDate;
    private String insuranceProvider;
    private String vehicleImageUrl;
    private VehicleStatus vehicleStatus;
}
