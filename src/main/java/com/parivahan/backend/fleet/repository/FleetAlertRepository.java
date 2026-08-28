package com.parivahan.backend.fleet.repository;

import com.parivahan.backend.fleet.domain.FleetAlert;
import com.parivahan.backend.fleet.enums.FleetAlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetAlertRepository extends JpaRepository<FleetAlert, Long> {
    List<FleetAlert> findByFleetIdOrderByCreatedAtDesc(Long fleetId);
    List<FleetAlert> findByFleetIdAndStatus(Long fleetId, String status);
    Optional<FleetAlert> findByFleetIdAndVehicleIdAndAlertTypeAndStatus(
            Long fleetId, Long vehicleId, FleetAlertType alertType, String status);
    long countByFleetIdAndStatus(Long fleetId, String status);
}
