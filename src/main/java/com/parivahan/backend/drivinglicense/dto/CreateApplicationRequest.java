package com.parivahan.backend.drivinglicense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 100, message = "State name must be between 2 and 100 characters")
    private String state;

    @NotBlank(message = "State code is required")
    @Size(min = 2, max = 10)
    private String stateCode;
}
