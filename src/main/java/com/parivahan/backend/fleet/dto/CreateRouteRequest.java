package com.parivahan.backend.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRouteRequest {
    @NotBlank
    private String vehicleRegistrationNumber;
    @NotBlank
    private String startLocation;
    @NotBlank
    private String destination;
    private Double startLat;
    private Double startLng;
    private Double destLat;
    private Double destLng;
    private Integer toleranceMeters; // defaults to 500
}
