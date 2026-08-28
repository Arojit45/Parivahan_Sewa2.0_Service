package com.parivahan.backend.vehicleregistration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVrRequest {
    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "State code is required")
    private String stateCode;
}
