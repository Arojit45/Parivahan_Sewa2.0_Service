package com.parivahan.backend.mydocuments.dto;

import com.parivahan.backend.vehicle.enums.VehicleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MyVehicleDocumentDto {
    private Long id;
    private String registrationNumber;
    private String nickname;
    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String registrationDate;
    private String rto;
    private String insuranceProvider;
    private String vehicleImageUrl;
    private LocalDate insuranceValidTill;
    private LocalDate pucValidTill;
    private LocalDate taxValidTill;
    private LocalDate permitValidTill;
    private LocalDate fitnessValidTill;
    private VehicleStatus vehicleStatus;
}
