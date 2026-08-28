package com.parivahan.backend.livelocation.service;

import com.parivahan.backend.livelocation.dto.VehicleTwinDto;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleLocationService {

    private final VehicleLocationRepository vehicleLocationRepository;

    @Transactional(readOnly = true)
    public VehicleTwinDto getLocationByVehicleId(Long vehicleId) {
        return vehicleLocationRepository.findByVehicleId(vehicleId)
                .map(loc -> VehicleTwinDto.builder()
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .speed(loc.getSpeed())
                        .heading(loc.getHeading())
                        .address(loc.getAddress())
                        .lastUpdated(loc.getLastUpdated())
                        .build())
                .orElse(null);
    }
}
