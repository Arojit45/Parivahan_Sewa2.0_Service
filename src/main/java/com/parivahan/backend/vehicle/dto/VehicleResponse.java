package com.parivahan.backend.vehicle.dto;

import com.parivahan.backend.vehicle.enums.VehicleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponse {
    private Long id;
    private String registrationNumber;
    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String registrationDate;
    private String rto;
    private VehicleStatus vehicleStatus;
}
