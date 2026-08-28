package com.parivahan.backend.fleet.service;

import com.parivahan.backend.fleet.domain.FleetAlert;
import com.parivahan.backend.fleet.domain.FleetRegistration;
import com.parivahan.backend.fleet.domain.FleetRoute;
import com.parivahan.backend.fleet.domain.FleetVehicle;
import com.parivahan.backend.fleet.dto.*;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import com.parivahan.backend.fleet.repository.*;
import com.parivahan.backend.livelocation.entity.VehicleLocation;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetDashboardService {

    private final FleetService fleetService;
    private final FleetVehicleRepository fleetVehicleRepo;
    private final FleetRouteRepository fleetRouteRepo;
    private final FleetAlertRepository fleetAlertRepo;
    private final VehicleLocationRepository locationRepo;

    @Transactional(readOnly = true)
    public FleetDashboardResponse getDashboard(Long fleetId) {
        FleetRegistration fleet = fleetService.getAuthorizedFleet(fleetId);

        List<FleetVehicle> fleetVehicles = fleetVehicleRepo.findByFleetIdAndActiveTrue(fleetId);
        List<FleetRoute> activeRoutes = fleetRouteRepo.findByFleetIdAndRouteStatus(fleetId, FleetRouteStatus.ACTIVE);
        List<FleetAlert> openAlerts = fleetAlertRepo.findByFleetIdAndStatus(fleetId, "OPEN");

        // Map vehicles with live GPS data
        List<FleetVehicleDto> vehicleDtos = fleetVehicles.stream().map(fv -> {
            VehicleLocation loc = locationRepo.findByVehicleId(fv.getVehicle().getId()).orElse(null);
            FleetRoute activeRoute = activeRoutes.stream()
                    .filter(r -> r.getVehicle().getId().equals(fv.getVehicle().getId()))
                    .findFirst().orElse(null);
            boolean hasAlert = openAlerts.stream()
                    .anyMatch(a -> a.getVehicle().getId().equals(fv.getVehicle().getId()));
            return fleetService.toVehicleDto(fv, loc, activeRoute, hasAlert);
        }).collect(Collectors.toList());

        int online = (int) vehicleDtos.stream().filter(v -> "ONLINE".equals(v.getOnlineStatus())).count();
        int offline = (int) vehicleDtos.stream().filter(v -> "OFFLINE".equals(v.getOnlineStatus())).count();

        // Map routes with live location
        List<FleetRouteDto> routeList = activeRoutes.stream().map(r -> {
            VehicleLocation loc = locationRepo.findByVehicleId(r.getVehicle().getId()).orElse(null);
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
        }).collect(Collectors.toList());

        List<FleetAlertDto> alertDtos = fleetAlertRepo.findByFleetIdOrderByCreatedAtDesc(fleetId)
                .stream().map(fleetService::toAlertDto).collect(Collectors.toList());

        return FleetDashboardResponse.builder()
                .fleetId(fleet.getId())
                .fleetName(fleet.getFleetName())
                .fleetRegistrationNumber(fleet.getFleetRegistrationNumber())
                .totalVehicles(fleetVehicles.size())
                .onlineVehicles(online)
                .offlineVehicles(offline)
                .activeRoutes(activeRoutes.size())
                .openAlerts((int) fleetAlertRepo.countByFleetIdAndStatus(fleetId, "OPEN"))
                .vehicles(vehicleDtos)
                .routes(routeList)
                .alerts(alertDtos)
                .build();
    }
}
