package com.parivahan.backend.fleet.service;

import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.fleet.domain.*;
import com.parivahan.backend.fleet.dto.*;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import com.parivahan.backend.fleet.enums.FleetStatus;
import com.parivahan.backend.fleet.repository.*;
import com.parivahan.backend.livelocation.entity.VehicleLocation;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.domain.Vehicle;
import com.parivahan.backend.vehicle.enums.VehicleStatus;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetService {

    private final FleetRegistrationRepository fleetRepo;
    private final FleetVehicleRepository fleetVehicleRepo;
    private final FleetRouteRepository fleetRouteRepo;
    private final FleetAlertRepository fleetAlertRepo;
    private final VehicleRepository vehicleRepo;
    private final VehicleLocationRepository locationRepo;
    private final UserRepository userRepo;

    // -----------------------------------------------------------------------
    // Fleet Registration
    // -----------------------------------------------------------------------

    @Transactional
    public FleetRegistrationResponse registerFleet(FleetRegistrationRequest req) {
        User owner = getCurrentUser();

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepo.findByRegistrationNumber(req.getVehicleRegistrationNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found. Please check the registration number."));

        // Ensure vehicle belongs to the current user
        if (!vehicle.getUser().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Vehicle is not available.");
        }

        // No duplicate PENDING or UNDER_REVIEW fleet allowed
        if (fleetRepo.existsByOwnerIdAndStatusIn(owner.getId(),
                List.of(FleetStatus.PENDING, FleetStatus.UNDER_REVIEW))) {
            throw new IllegalArgumentException("You already have a fleet application under review.");
        }

        FleetRegistration fleet = FleetRegistration.builder()
                .owner(owner)
                .fleetName(req.getFleetName())
                .vehicleRegistrationNumber(req.getVehicleRegistrationNumber())
                .status(FleetStatus.PENDING)
                .document1Base64(req.getDocument1Base64())
                .document2Base64(req.getDocument2Base64())
                .businessProofBase64(req.getBusinessProofBase64())
                .build();

        fleet = fleetRepo.save(fleet);
        return toRegistrationResponse(fleet);
    }

    @Transactional(readOnly = true)
    public List<FleetRegistrationResponse> getMyFleets() {
        User owner = getCurrentUser();
        return fleetRepo.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream().map(this::toRegistrationResponse).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Fleet Vehicle Management
    // -----------------------------------------------------------------------

    @Transactional
    public FleetVehicleDto addVehicleToFleet(Long fleetId, AddFleetVehicleRequest req) {
        FleetRegistration fleet = getAuthorizedFleet(fleetId);
        assertApproved(fleet);

        Vehicle vehicle = vehicleRepo.findByRegistrationNumber(req.getRegistrationNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found."));

        if (!vehicle.getUser().getId().equals(getCurrentUser().getId())) {
            throw new IllegalArgumentException("Vehicle is not available.");
        }

        if (fleetVehicleRepo.existsByFleetIdAndVehicleIdAndActiveTrue(fleetId, vehicle.getId())) {
            throw new IllegalArgumentException("Vehicle is already in this fleet.");
        }

        FleetVehicle fv = FleetVehicle.builder()
                .fleet(fleet)
                .vehicle(vehicle)
                .active(true)
                .addedAt(LocalDateTime.now())
                .build();

        fv = fleetVehicleRepo.save(fv);
        return toVehicleDto(fv, locationRepo.findByVehicleId(vehicle.getId()).orElse(null),
                getActiveRoute(fleetId, vehicle.getId()), hasOpenAlert(fleetId, vehicle.getId()));
    }

    @Transactional
    public void removeVehicleFromFleet(Long fleetId, Long vehicleId) {
        getAuthorizedFleet(fleetId);
        FleetVehicle fv = fleetVehicleRepo.findByFleetIdAndVehicleId(fleetId, vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in fleet."));
        fv.setActive(false);
        fleetVehicleRepo.save(fv);
    }

    // -----------------------------------------------------------------------
    // Route Management
    // -----------------------------------------------------------------------

    @Transactional
    public FleetRouteDto createRoute(Long fleetId, CreateRouteRequest req) {
        FleetRegistration fleet = getAuthorizedFleet(fleetId);
        assertApproved(fleet);

        Vehicle vehicle = vehicleRepo.findByRegistrationNumber(req.getVehicleRegistrationNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found."));

        // Check no active route for this vehicle
        if (fleetRouteRepo.findByVehicleIdAndRouteStatus(vehicle.getId(), FleetRouteStatus.ACTIVE).isPresent()) {
            throw new IllegalArgumentException("Vehicle already has an active route. Stop it first.");
        }

        FleetRoute route = FleetRoute.builder()
                .fleet(fleet)
                .vehicle(vehicle)
                .startLocation(req.getStartLocation())
                .destination(req.getDestination())
                .startLat(req.getStartLat())
                .startLng(req.getStartLng())
                .destLat(req.getDestLat())
                .destLng(req.getDestLng())
                .toleranceMeters(req.getToleranceMeters() != null ? req.getToleranceMeters() : 500)
                .routeStatus(FleetRouteStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();

        route = fleetRouteRepo.save(route);
        VehicleLocation loc = locationRepo.findByVehicleId(vehicle.getId()).orElse(null);
        return toRouteDto(route, loc);
    }

    @Transactional
    public FleetRouteDto stopRoute(Long fleetId, Long routeId) {
        getAuthorizedFleet(fleetId);
        FleetRoute route = fleetRouteRepo.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found."));
        if (!route.getFleet().getId().equals(fleetId)) {
            throw new SecurityException("Access denied.");
        }
        route.setRouteStatus(FleetRouteStatus.COMPLETED);
        route.setCompletedAt(LocalDateTime.now());
        route = fleetRouteRepo.save(route);
        VehicleLocation loc = locationRepo.findByVehicleId(route.getVehicle().getId()).orElse(null);
        return toRouteDto(route, loc);
    }

    @Transactional(readOnly = true)
    public List<FleetRouteDto> getActiveRoutes(Long fleetId) {
        getAuthorizedFleet(fleetId);
        return fleetRouteRepo.findByFleetIdAndRouteStatus(fleetId, FleetRouteStatus.ACTIVE)
                .stream().map(r -> {
                    VehicleLocation loc = locationRepo.findByVehicleId(r.getVehicle().getId()).orElse(null);
                    return toRouteDto(r, loc);
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FleetAlertDto> getFleetAlerts(Long fleetId) {
        getAuthorizedFleet(fleetId);
        return fleetAlertRepo.findByFleetIdOrderByCreatedAtDesc(fleetId)
                .stream().map(this::toAlertDto).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Authorization helper — validates JWT user owns this fleet
    // -----------------------------------------------------------------------

    public FleetRegistration getAuthorizedFleet(Long fleetId) {
        FleetRegistration fleet = fleetRepo.findById(fleetId)
                .orElseThrow(() -> new ResourceNotFoundException("Fleet not found."));
        if (!fleet.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied: You do not own this fleet.");
        }
        return fleet;
    }

    private void assertApproved(FleetRegistration fleet) {
        if (fleet.getStatus() != FleetStatus.APPROVED) {
            throw new IllegalArgumentException("Fleet is not yet approved.");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private FleetRoute getActiveRoute(Long fleetId, Long vehicleId) {
        return fleetRouteRepo.findByVehicleIdAndRouteStatus(vehicleId, FleetRouteStatus.ACTIVE)
                .filter(r -> r.getFleet().getId().equals(fleetId))
                .orElse(null);
    }

    private boolean hasOpenAlert(Long fleetId, Long vehicleId) {
        return fleetAlertRepo.findByFleetIdOrderByCreatedAtDesc(fleetId).stream()
                .anyMatch(a -> a.getVehicle().getId().equals(vehicleId) && "OPEN".equals(a.getStatus()));
    }

    private String deriveOnlineStatus(VehicleLocation loc, boolean hasAlert) {
        if (loc == null) return "OFFLINE";
        long minutesSince = java.time.Duration.between(loc.getLastUpdated(), LocalDateTime.now()).toMinutes();
        if (minutesSince > 10) return "OFFLINE";
        if (hasAlert) return "AT_RISK";
        return "ONLINE";
    }

    // -----------------------------------------------------------------------
    // Mappers
    // -----------------------------------------------------------------------

    private FleetRegistrationResponse toRegistrationResponse(FleetRegistration f) {
        return FleetRegistrationResponse.builder()
                .id(f.getId())
                .fleetName(f.getFleetName())
                .fleetRegistrationNumber(f.getFleetRegistrationNumber())
                .status(f.getStatus())
                .rejectionReason(f.getRejectionReason())
                .vehicleRegistrationNumber(f.getVehicleRegistrationNumber())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    public FleetVehicleDto toVehicleDto(FleetVehicle fv, VehicleLocation loc, FleetRoute activeRoute, boolean hasAlert) {
        Vehicle v = fv.getVehicle();
        String routeInfo = activeRoute != null
                ? activeRoute.getStartLocation() + " → " + activeRoute.getDestination() : null;
        return FleetVehicleDto.builder()
                .id(fv.getId())
                .vehicleId(v.getId())
                .registrationNumber(v.getRegistrationNumber())
                .nickname(v.getNickname())
                .manufacturer(v.getManufacturer())
                .model(v.getModel())
                .fuelType(v.getFuelType())
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .speed(loc != null ? loc.getSpeed() : null)
                .heading(loc != null ? loc.getHeading() : null)
                .address(loc != null ? loc.getAddress() : null)
                .lastUpdated(loc != null ? loc.getLastUpdated() : null)
                .onlineStatus(deriveOnlineStatus(loc, hasAlert))
                .routeInfo(routeInfo)
                .hasAlert(hasAlert)
                .build();
    }

    private FleetRouteDto toRouteDto(FleetRoute r, VehicleLocation loc) {
        return FleetRouteDto.builder()
                .id(r.getId())
                .vehicleId(r.getVehicle().getId())
                .vehicleRegistrationNumber(r.getVehicle().getRegistrationNumber())
                .startLocation(r.getStartLocation())
                .destination(r.getDestination())
                .routeStatus(r.getRouteStatus())
                .toleranceMeters(r.getToleranceMeters())
                .startedAt(r.getStartedAt())
                .completedAt(r.getCompletedAt())
                .currentLat(loc != null ? loc.getLatitude() : null)
                .currentLng(loc != null ? loc.getLongitude() : null)
                .currentAddress(loc != null ? loc.getAddress() : null)
                .speed(loc != null ? loc.getSpeed() : null)
                .build();
    }

    public FleetAlertDto toAlertDto(FleetAlert a) {
        return FleetAlertDto.builder()
                .id(a.getId())
                .vehicleId(a.getVehicle().getId())
                .vehicleRegistrationNumber(a.getVehicle().getRegistrationNumber())
                .alertType(a.getAlertType())
                .message(a.getMessage())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
