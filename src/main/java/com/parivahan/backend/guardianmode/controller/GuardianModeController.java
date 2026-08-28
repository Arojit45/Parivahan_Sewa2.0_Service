package com.parivahan.backend.guardianmode.controller;

import com.parivahan.backend.guardianmode.dto.GeofenceBreachEventDto;
import com.parivahan.backend.guardianmode.dto.GuardianConfigDto;
import com.parivahan.backend.guardianmode.dto.GuardianConfigRequest;
import com.parivahan.backend.guardianmode.service.GeofenceCheckerService;
import com.parivahan.backend.guardianmode.service.GuardianModeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/guardian-mode")
@RequiredArgsConstructor
public class GuardianModeController {

    private final GuardianModeService guardianModeService;
    private final GeofenceCheckerService geofenceCheckerService;

    /**
     * GET /api/v1/guardian-mode/vehicles/{vehicleId}
     * Returns current Guardian Mode config for the vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<GuardianConfigDto> getConfig(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(guardianModeService.getConfig(vehicleId));
    }

    /**
     * POST /api/v1/guardian-mode/vehicles/{vehicleId}
     * Create or update the safe zone config.
     * Accepts either { "safeAreaName": "MG Road Pune", "radiusMeters": 2000 }
     *             or { "safeLat": 18.52, "safeLng": 73.87, "radiusMeters": 5000 }
     */
    @PostMapping("/vehicles/{vehicleId}")
    public ResponseEntity<GuardianConfigDto> saveConfig(
            @PathVariable Long vehicleId,
            @Valid @RequestBody GuardianConfigRequest request) {
        return ResponseEntity.ok(guardianModeService.saveConfig(vehicleId, request));
    }

    /**
     * PATCH /api/v1/guardian-mode/vehicles/{vehicleId}/toggle
     * Enable or disable Guardian Mode. Returns the updated config.
     */
    @PatchMapping("/vehicles/{vehicleId}/toggle")
    public ResponseEntity<GuardianConfigDto> toggle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(guardianModeService.toggle(vehicleId));
    }

    /**
     * POST /api/v1/guardian-mode/vehicles/{vehicleId}/check
     * Manually trigger a geofence check against the vehicle's current live location.
     * Returns the breach event if breached, or a safe message if within zone.
     */
    @PostMapping("/vehicles/{vehicleId}/check")
    public ResponseEntity<?> checkGeofence(@PathVariable Long vehicleId) {
        GeofenceBreachEventDto breach = geofenceCheckerService.checkGeofence(vehicleId);
        if (breach == null) {
            return ResponseEntity.ok(Map.of("status", "SAFE", "message", "Vehicle is within the safe zone."));
        }
        return ResponseEntity.ok(breach);
    }

    /**
     * GET /api/v1/guardian-mode/vehicles/{vehicleId}/breach-events
     * Returns breach history for the vehicle timeline and live map.
     */
    @GetMapping("/vehicles/{vehicleId}/breach-events")
    public ResponseEntity<List<GeofenceBreachEventDto>> getBreachHistory(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(geofenceCheckerService.getBreachHistory(vehicleId));
    }
}
