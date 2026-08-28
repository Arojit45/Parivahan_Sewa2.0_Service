package com.parivahan.backend.vehicle.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class VehiclePublicResponse {
    private String registrationNumber;
    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String rto;
}
