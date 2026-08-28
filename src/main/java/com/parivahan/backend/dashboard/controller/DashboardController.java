package com.parivahan.backend.dashboard.controller;

import com.parivahan.backend.dashboard.dto.DashboardResponseDto;
import com.parivahan.backend.dashboard.dto.VehicleCardDto;
import com.parivahan.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Returns the full dashboard for a single vehicle.
     * Only the authenticated owner can access their vehicle's dashboard.
     *
     * GET /api/v1/dashboard/vehicles/{vehicleId}
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<DashboardResponseDto> getVehicleDashboard(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(dashboardService.getDashboard(vehicleId));
    }

    /**
     * Returns a lightweight card list of all vehicles owned by the current user.
     * Used for the "My Vehicles" sidebar.
     *
     * GET /api/v1/dashboard/vehicles
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleCardDto>> getMyVehicles() {
        return ResponseEntity.ok(dashboardService.getAllVehicleCards());
    }
}
