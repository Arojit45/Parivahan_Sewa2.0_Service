package com.parivahan.backend.fleet.repository;

import com.parivahan.backend.fleet.domain.FleetRoute;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetRouteRepository extends JpaRepository<FleetRoute, Long> {
    List<FleetRoute> findByFleetIdAndRouteStatus(Long fleetId, FleetRouteStatus status);
    List<FleetRoute> findByFleetId(Long fleetId);
    Optional<FleetRoute> findByVehicleIdAndRouteStatus(Long vehicleId, FleetRouteStatus status);
    List<FleetRoute> findByRouteStatus(FleetRouteStatus status);
}
