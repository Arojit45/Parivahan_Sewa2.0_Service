package com.parivahan.backend.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFleetVehicleRequest {
    @NotBlank(message = "Vehicle registration number is required")
    private String registrationNumber;
}
