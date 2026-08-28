package com.parivahan.backend.fleet.service;

import com.parivahan.backend.fleet.domain.FleetAlert;
import com.parivahan.backend.fleet.domain.FleetRoute;
import com.parivahan.backend.fleet.domain.FleetVehicle;
import com.parivahan.backend.fleet.enums.FleetAlertType;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import com.parivahan.backend.fleet.repository.*;
import com.parivahan.backend.livelocation.entity.VehicleLocation;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service that runs every 60 seconds to detect:
 * 1. Route Deviation: Vehicle GPS outside route corridor
 * 2. GPS Offline: No GPS update for > 10 minutes
 * 3. GPS Restored: Previously offline vehicle back online
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FleetMonitoringService {

    private final FleetVehicleRepository fleetVehicleRepo;
    private final FleetRouteRepository fleetRouteRepo;
    private final FleetAlertRepository fleetAlertRepo;
    private final VehicleLocationRepository locationRepo;

    private static final int GPS_OFFLINE_THRESHOLD_MINUTES = 10;
    private static final int ALERT_COOLDOWN_MINUTES = 5; // Don't re-trigger alert within 5 min

    @Scheduled(fixedDelay = 60_000) // every 60 seconds
    @Transactional
    public void runMonitoring() {
        checkRouteDeviations();
        checkGpsOffline();
    }

    // -----------------------------------------------------------------------
    // Route Deviation
    // -----------------------------------------------------------------------
    private void checkRouteDeviations() {
        List<FleetRoute> activeRoutes = fleetRouteRepo.findByRouteStatus(FleetRouteStatus.ACTIVE);
        for (FleetRoute route : activeRoutes) {
            VehicleLocation loc = locationRepo.findByVehicleId(route.getVehicle().getId()).orElse(null);
            if (loc == null || route.getStartLat() == null || route.getDestLat() == null) continue;

            double deviation = distanceToSegmentMeters(
                    loc.getLatitude(), loc.getLongitude(),
                    route.getStartLat(), route.getStartLng(),
                    route.getDestLat(), route.getDestLng());

            Long fleetId = route.getFleet().getId();
            Long vehicleId = route.getVehicle().getId();

            if (deviation > route.getToleranceMeters()) {
                // Check cooldown before creating/updating alert
                var existingOpt = fleetAlertRepo.findByFleetIdAndVehicleIdAndAlertTypeAndStatus(
                        fleetId, vehicleId, FleetAlertType.ROUTE_DEVIATION, "OPEN");

                if (existingOpt.isEmpty() || withinCooldown(existingOpt.get().getLastTriggeredAt())) {
                    FleetAlert alert = existingOpt.orElse(FleetAlert.builder()
                            .fleet(route.getFleet())
                            .vehicle(route.getVehicle())
                            .alertType(FleetAlertType.ROUTE_DEVIATION)
                            .status("OPEN")
                            .build());
                    alert.setMessage(String.format(
                            "Vehicle %s has deviated %.0fm from assigned route %s → %s.",
                            route.getVehicle().getRegistrationNumber(), deviation,
                            route.getStartLocation(), route.getDestination()));
                    alert.setLastTriggeredAt(LocalDateTime.now());
                    fleetAlertRepo.save(alert);
                    log.info("[FleetMonitor] Route deviation: vehicle={} deviation={}m", vehicleId, (int) deviation);
                }
            } else {
                // Vehicle back on route — resolve open deviation alert
                fleetAlertRepo.findByFleetIdAndVehicleIdAndAlertTypeAndStatus(
                        fleetId, vehicleId, FleetAlertType.ROUTE_DEVIATION, "OPEN")
                        .ifPresent(alert -> {
                            alert.setStatus("RESOLVED");
                            alert.setResolvedAt(LocalDateTime.now());
                            alert.setMessage(alert.getMessage() + " ✓ Vehicle returned to route.");
                            fleetAlertRepo.save(alert);
                            log.info("[FleetMonitor] Vehicle {} back on route.", vehicleId);
                        });
            }
        }
    }

    // -----------------------------------------------------------------------
    // GPS Offline Detection
    // -----------------------------------------------------------------------
    private void checkGpsOffline() {
        // Get all fleet vehicles from all fleets
        List<FleetVehicle> allFleetVehicles = fleetVehicleRepo.findAll().stream()
                .filter(FleetVehicle::isActive).toList();

        for (FleetVehicle fv : allFleetVehicles) {
            VehicleLocation loc = locationRepo.findByVehicleId(fv.getVehicle().getId()).orElse(null);
            Long fleetId = fv.getFleet().getId();
            Long vehicleId = fv.getVehicle().getId();

            boolean isOffline = loc == null ||
                    Duration.between(loc.getLastUpdated(), LocalDateTime.now()).toMinutes() >= GPS_OFFLINE_THRESHOLD_MINUTES;

            var offlineAlert = fleetAlertRepo.findByFleetIdAndVehicleIdAndAlertTypeAndStatus(
                    fleetId, vehicleId, FleetAlertType.GPS_OFFLINE, "OPEN");

            if (isOffline) {
                if (offlineAlert.isEmpty() || withinCooldown(offlineAlert.get().getLastTriggeredAt())) {
                    FleetAlert alert = offlineAlert.orElse(FleetAlert.builder()
                            .fleet(fv.getFleet())
                            .vehicle(fv.getVehicle())
                            .alertType(FleetAlertType.GPS_OFFLINE)
                            .status("OPEN")
                            .build());
                    long minAgo = loc != null
                            ? Duration.between(loc.getLastUpdated(), LocalDateTime.now()).toMinutes() : -1;
                    alert.setMessage(String.format(
                            "Vehicle %s GPS offline. Last seen %s minutes ago.",
                            fv.getVehicle().getRegistrationNumber(),
                            minAgo >= 0 ? minAgo : "unknown"));
                    alert.setLastTriggeredAt(LocalDateTime.now());
                    fleetAlertRepo.save(alert);
                }
            } else {
                // GPS is back — resolve offline alert
                offlineAlert.ifPresent(alert -> {
                    alert.setStatus("RESOLVED");
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setMessage(alert.getMessage() + " ✓ GPS restored.");
                    fleetAlertRepo.save(alert);
                    log.info("[FleetMonitor] Vehicle {} GPS restored.", vehicleId);
                });
            }
        }
    }

    // -----------------------------------------------------------------------
    // Math Helpers
    // -----------------------------------------------------------------------

    /** Haversine distance in meters from a point to a line segment */
    private double distanceToSegmentMeters(double pLat, double pLng,
                                            double aLat, double aLng,
                                            double bLat, double bLng) {
        double t = ((pLat - aLat) * (bLat - aLat) + (pLng - aLng) * (bLng - aLng))
                / ((bLat - aLat) * (bLat - aLat) + (bLng - aLng) * (bLng - aLng) + 1e-10);
        t = Math.max(0, Math.min(1, t));
        double closestLat = aLat + t * (bLat - aLat);
        double closestLng = aLng + t * (bLng - aLng);
        return haversineMeters(pLat, pLng, closestLat, closestLng);
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6_371_000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private boolean withinCooldown(LocalDateTime lastTriggered) {
        if (lastTriggered == null) return false;
        return Duration.between(lastTriggered, LocalDateTime.now()).toMinutes() < ALERT_COOLDOWN_MINUTES;
    }
}
