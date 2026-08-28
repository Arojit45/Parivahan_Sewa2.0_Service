package com.parivahan.backend.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FleetRegistrationRequest {
    @NotBlank(message = "Fleet name is required")
    private String fleetName;

    @NotBlank(message = "Vehicle registration number is required")
    private String vehicleRegistrationNumber;

    private String document1Base64;
    private String document2Base64;
    private String businessProofBase64;
}
