package com.parivahan.backend.guardianmode.service;

import com.parivahan.backend.alertsystem.enums.AlertSeverity;
import com.parivahan.backend.alertsystem.enums.AlertType;
import com.parivahan.backend.alertsystem.service.AlertService;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.guardianmode.dto.GeofenceBreachEventDto;
import com.parivahan.backend.guardianmode.entity.GeofenceBreachEvent;
import com.parivahan.backend.guardianmode.entity.GuardianConfig;
import com.parivahan.backend.guardianmode.repository.GeofenceBreachEventRepository;
import com.parivahan.backend.guardianmode.repository.GuardianConfigRepository;
import com.parivahan.backend.livelocation.entity.VehicleLocation;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.domain.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceCheckerService {

    private final GuardianConfigRepository guardianConfigRepository;
    private final GeofenceBreachEventRepository breachEventRepository;
    private final VehicleLocationRepository vehicleLocationRepository;
    private final AlertService alertService;
    private final UserRepository userRepository;

    /**
     * Checks if the vehicle's current location is outside its configured safe zone.
     * Saves a GeofenceBreachEvent and dispatches an alert + SMS if breached.
     *
     * @return breach event DTO if breached, null if within safe zone
     */
    @Transactional
    public GeofenceBreachEventDto checkGeofence(Long vehicleId) {
        GuardianConfig config = guardianConfigRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Guardian Mode not configured for this vehicle"));
        verifyOwner(config.getVehicle());

        if (!config.isEnabled()) {
            throw new IllegalStateException("Guardian Mode is not enabled for this vehicle");
        }

        VehicleLocation location = vehicleLocationRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("No live location data available for this vehicle"));

        double distance = haversineDistance(
                config.getSafeLat(), config.getSafeLng(),
                location.getLatitude(), location.getLongitude()
        );

        log.info("Geofence check — vehicleId={} distance={:.1f}m radius={}m",
                vehicleId, distance, config.getRadiusMeters());

        if (distance <= config.getRadiusMeters()) {
            return null; // within safe zone — no breach
        }

        // --- BREACH DETECTED ---
        Vehicle vehicle = config.getVehicle();

        GeofenceBreachEvent event = GeofenceBreachEvent.builder()
                .vehicle(vehicle)
                .breachLat(location.getLatitude())
                .breachLng(location.getLongitude())
                .distanceFromSafeZone(distance)
                .lastKnownAddress(location.getAddress())
                .breachedAt(LocalDateTime.now())
                .build();
        event = breachEventRepository.save(event);

        // Update last breach time on config
        config.setLastBreachAt(event.getBreachedAt());
        guardianConfigRepository.save(config);

        // Dispatch alert (IN_APP + SMS)
        String title = "Guardian Mode: Geofence Breach!";
        String message = String.format(
                "Vehicle %s has moved %.0f meters outside its safe zone. Last known location: %s",
                vehicle.getRegistrationNumber(), distance, location.getAddress()
        );
        alertService.send(config.getUser(), AlertType.GEOFENCE_BREACH, AlertSeverity.CRITICAL,
                title, message, vehicle.getId(), true);

        return toDto(event);
    }

    @Transactional(readOnly = true)
    public List<GeofenceBreachEventDto> getBreachHistory(Long vehicleId) {
        GuardianConfig config = guardianConfigRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Guardian Mode not configured for this vehicle"));
        verifyOwner(config.getVehicle());

        return breachEventRepository.findByVehicleIdOrderByBreachedAtDesc(vehicleId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Haversine formula — accurate great-circle distance in meters
    // -----------------------------------------------------------------------
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_M = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private GeofenceBreachEventDto toDto(GeofenceBreachEvent e) {
        return GeofenceBreachEventDto.builder()
                .id(e.getId())
                .vehicleId(e.getVehicle().getId())
                .registrationNumber(e.getVehicle().getRegistrationNumber())
                .breachLat(e.getBreachLat())
                .breachLng(e.getBreachLng())
                .distanceFromSafeZone(e.getDistanceFromSafeZone())
                .lastKnownAddress(e.getLastKnownAddress())
                .breachedAt(e.getBreachedAt())
                .build();
    }

    private void verifyOwner(Vehicle vehicle) {
        if (!vehicle.getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied: You do not own this vehicle");
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
