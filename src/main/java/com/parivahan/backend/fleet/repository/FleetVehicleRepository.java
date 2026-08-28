package com.parivahan.backend.fleet.repository;

import com.parivahan.backend.fleet.domain.FleetVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetVehicleRepository extends JpaRepository<FleetVehicle, Long> {
    List<FleetVehicle> findByFleetIdAndActiveTrue(Long fleetId);
    Optional<FleetVehicle> findByFleetIdAndVehicleId(Long fleetId, Long vehicleId);
    boolean existsByFleetIdAndVehicleIdAndActiveTrue(Long fleetId, Long vehicleId);
}
