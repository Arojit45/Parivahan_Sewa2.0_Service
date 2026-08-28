package com.parivahan.backend.vehicle.dto;

import com.parivahan.backend.vehicle.enums.VehicleStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VehicleOwnerResponse extends VehiclePublicResponse {
    private Long id;
    private String registrationDate;
    private VehicleStatus vehicleStatus;
}
