package com.parivahan.backend.guardianmode.service;

import com.parivahan.backend.alertsystem.enums.AlertSeverity;
import com.parivahan.backend.alertsystem.enums.AlertType;
import com.parivahan.backend.alertsystem.service.AlertService;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.guardianmode.dto.GuardianConfigDto;
import com.parivahan.backend.guardianmode.dto.GuardianConfigRequest;
import com.parivahan.backend.guardianmode.entity.GuardianConfig;
import com.parivahan.backend.guardianmode.repository.GuardianConfigRepository;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.domain.Vehicle;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuardianModeService {

    private final GuardianConfigRepository guardianConfigRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final MockGeocoderService geocoderService;
    private final AlertService alertService;

    /** Get current guardian config for a vehicle. */
    @Transactional(readOnly = true)
    public GuardianConfigDto getConfig(Long vehicleId) {
        Vehicle vehicle = getOwnedVehicle(vehicleId);
        GuardianConfig config = guardianConfigRepository.findByVehicleId(vehicleId)
                .orElse(GuardianConfig.builder()
                        .vehicle(vehicle).user(vehicle.getUser()).enabled(false).radiusMeters(2000.0).build());
        return toDto(config);
    }

    /** Create or update the guardian config for a vehicle. */
    @Transactional
    public GuardianConfigDto saveConfig(Long vehicleId, GuardianConfigRequest request) {
        User user = getCurrentUser();
        Vehicle vehicle = getOwnedVehicle(vehicleId);

        GuardianConfig config = guardianConfigRepository.findByVehicleId(vehicleId)
                .orElse(GuardianConfig.builder().vehicle(vehicle).user(user).build());

        // Resolve area name to coordinates if provided
        if (request.getSafeAreaName() != null && !request.getSafeAreaName().isBlank()) {
            double[] coords = geocoderService.resolve(request.getSafeAreaName());
            if (coords == null) {
                throw new IllegalArgumentException(
                        "Area name '" + request.getSafeAreaName() + "' could not be resolved. " +
                        "Try a well-known area name or provide coordinates directly.");
            }
            config.setSafeLat(coords[0]);
            config.setSafeLng(coords[1]);
            config.setSafeAreaName(request.getSafeAreaName());
        } else if (request.getSafeLat() != null && request.getSafeLng() != null) {
            config.setSafeLat(request.getSafeLat());
            config.setSafeLng(request.getSafeLng());
            config.setSafeAreaName(null);
        } else if (config.getSafeLat() == null || config.getSafeLng() == null) {
            throw new IllegalArgumentException("Provide either an area name or lat/lng coordinates.");
        }

        if (request.getRadiusMeters() != null) {
            config.setRadiusMeters(request.getRadiusMeters());
        }
        if (request.getPushAlertsEnabled() != null) {
            config.setPushAlertsEnabled(request.getPushAlertsEnabled());
        }
        if (request.getSmsAlertsEnabled() != null) {
            config.setSmsAlertsEnabled(request.getSmsAlertsEnabled());
        }
        if (request.getEmailAlertsEnabled() != null) {
            config.setEmailAlertsEnabled(request.getEmailAlertsEnabled());
        }
        if (request.getQuietHoursEnabled() != null) {
            config.setQuietHoursEnabled(request.getQuietHoursEnabled());
        }
        if (request.getQuietHoursStart() != null && !request.getQuietHoursStart().isBlank()) {
            config.setQuietHoursStart(request.getQuietHoursStart());
        }
        if (request.getQuietHoursEnd() != null && !request.getQuietHoursEnd().isBlank()) {
            config.setQuietHoursEnd(request.getQuietHoursEnd());
        }
        config.setUser(user);

        return toDto(guardianConfigRepository.save(config));
    }

    /** Toggle Guardian Mode on/off. Returns the updated config. */
    @Transactional
    public GuardianConfigDto toggle(Long vehicleId) {
        User user = getCurrentUser();
        getOwnedVehicle(vehicleId);

        GuardianConfig config = guardianConfigRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new IllegalStateException(
                        "Configure Guardian Mode first before enabling it."));

        if (!config.isEnabled() && (config.getSafeLat() == null || config.getSafeLng() == null)) {
            throw new IllegalStateException("Set a safe location before enabling Guardian Mode.");
        }

        config.setEnabled(!config.isEnabled());
        config = guardianConfigRepository.save(config);

        String action = config.isEnabled() ? "enabled" : "disabled";
        alertService.send(user, AlertType.GUARDIAN_MODE_ENABLED, AlertSeverity.INFO,
                "Guardian Mode " + action,
                "Guardian Mode has been " + action + " for vehicle " + config.getVehicle().getRegistrationNumber(),
                vehicleId, false);

        return toDto(config);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Vehicle getOwnedVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!vehicle.getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied: You do not own this vehicle");
        }
        return vehicle;
    }

    private GuardianConfigDto toDto(GuardianConfig c) {
        return GuardianConfigDto.builder()
                .vehicleId(c.getVehicle() != null ? c.getVehicle().getId() : null)
                .registrationNumber(c.getVehicle() != null ? c.getVehicle().getRegistrationNumber() : null)
                .enabled(c.isEnabled())
                .safeLat(c.getSafeLat())
                .safeLng(c.getSafeLng())
                .safeAreaName(c.getSafeAreaName())
                .radiusMeters(c.getRadiusMeters())
                .pushAlertsEnabled(c.isPushAlertsEnabled())
                .smsAlertsEnabled(c.isSmsAlertsEnabled())
                .emailAlertsEnabled(c.isEmailAlertsEnabled())
                .quietHoursEnabled(c.isQuietHoursEnabled())
                .quietHoursStart(c.getQuietHoursStart())
                .quietHoursEnd(c.getQuietHoursEnd())
                .lastBreachAt(c.getLastBreachAt())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
