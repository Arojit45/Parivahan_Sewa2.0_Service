package com.parivahan.backend.fleet.controller;

import com.parivahan.backend.fleet.dto.*;
import com.parivahan.backend.fleet.service.FleetDashboardService;
import com.parivahan.backend.fleet.service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fleet")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;
    private final FleetDashboardService fleetDashboardService;

    // -----------------------------------------------------------------------
    // Fleet Registration
    // -----------------------------------------------------------------------

    @PostMapping("/registrations")
    public ResponseEntity<FleetRegistrationResponse> registerFleet(
            @Valid @RequestBody FleetRegistrationRequest req) {
        return new ResponseEntity<>(fleetService.registerFleet(req), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FleetRegistrationResponse>> getMyFleets() {
        return ResponseEntity.ok(fleetService.getMyFleets());
    }

    // -----------------------------------------------------------------------
    // Fleet Dashboard
    // -----------------------------------------------------------------------

    @GetMapping("/{fleetId}/dashboard")
    public ResponseEntity<FleetDashboardResponse> getDashboard(@PathVariable Long fleetId) {
        return ResponseEntity.ok(fleetDashboardService.getDashboard(fleetId));
    }

    // -----------------------------------------------------------------------
    // Fleet Vehicle Management
    // -----------------------------------------------------------------------

    @PostMapping("/{fleetId}/vehicles")
    public ResponseEntity<FleetVehicleDto> addVehicle(
            @PathVariable Long fleetId,
            @Valid @RequestBody AddFleetVehicleRequest req) {
        return new ResponseEntity<>(fleetService.addVehicleToFleet(fleetId, req), HttpStatus.CREATED);
    }

    @DeleteMapping("/{fleetId}/vehicles/{vehicleId}")
    public ResponseEntity<Void> removeVehicle(
            @PathVariable Long fleetId,
            @PathVariable Long vehicleId) {
        fleetService.removeVehicleFromFleet(fleetId, vehicleId);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // Routes
    // -----------------------------------------------------------------------

    @PostMapping("/{fleetId}/routes")
    public ResponseEntity<FleetRouteDto> createRoute(
            @PathVariable Long fleetId,
            @Valid @RequestBody CreateRouteRequest req) {
        return new ResponseEntity<>(fleetService.createRoute(fleetId, req), HttpStatus.CREATED);
    }

    @GetMapping("/{fleetId}/routes/active")
    public ResponseEntity<List<FleetRouteDto>> getActiveRoutes(@PathVariable Long fleetId) {
        return ResponseEntity.ok(fleetService.getActiveRoutes(fleetId));
    }

    @PostMapping("/{fleetId}/routes/{routeId}/stop")
    public ResponseEntity<FleetRouteDto> stopRoute(
            @PathVariable Long fleetId,
            @PathVariable Long routeId) {
        return ResponseEntity.ok(fleetService.stopRoute(fleetId, routeId));
    }

    // -----------------------------------------------------------------------
    // Alerts
    // -----------------------------------------------------------------------

    @GetMapping("/{fleetId}/alerts")
    public ResponseEntity<List<FleetAlertDto>> getAlerts(@PathVariable Long fleetId) {
        return ResponseEntity.ok(fleetService.getFleetAlerts(fleetId));
    }
}
