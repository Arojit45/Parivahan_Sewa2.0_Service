package com.parivahan.backend.dashboard.service;

import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.dashboard.dto.*;
import com.parivahan.backend.dashboard.dto.ComplianceItemDto.ComplianceStatus;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.challan.service.ChallanService;
import com.parivahan.backend.livelocation.dto.VehicleTwinDto;
import com.parivahan.backend.livelocation.service.VehicleLocationService;
import com.parivahan.backend.vehicle.domain.Vehicle;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final ChallanService challanService;
    private final UserRepository userRepository;
    private final VehicleLocationService vehicleLocationService;
    private final HealthScoreCalculator healthScoreCalculator;
    private final AlertBuilder alertBuilder;

    /** Returns the full per-vehicle dashboard for the authenticated owner. */
    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboard(Long vehicleId) {
        User currentUser = getCurrentUser();
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied: You do not own this vehicle");
        }

        List<Challan> pendingChallans = challanService.getPendingChallansForVehicle(vehicleId);
        ComplianceStatusDto compliance = buildCompliance(vehicle);
        int healthScore = healthScoreCalculator.calculate(compliance);

        return DashboardResponseDto.builder()
                .vehicleCard(buildVehicleCard(vehicle, currentUser))
                .vehicleTwin(vehicleLocationService.getLocationByVehicleId(vehicleId))
                .compliance(compliance)
                .alerts(alertBuilder.build(compliance, pendingChallans))
                .pendingChallans(pendingChallans.stream().map(this::toChallanDto).collect(Collectors.toList()))
                .healthScore(healthScore)
                .healthLabel(healthScoreCalculator.getHealthLabel(healthScore))
                .build();
    }

    /**
     * Returns lightweight cards for all vehicles owned by the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<VehicleCardDto> getAllVehicleCards() {
        User currentUser = getCurrentUser();
        return vehicleRepository.findByUserId(currentUser.getId())
                .stream()
                .filter(v -> v.getVehicleStatus() == com.parivahan.backend.vehicle.enums.VehicleStatus.ACTIVE)
                .map(v -> buildVehicleCard(v, currentUser))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private VehicleCardDto buildVehicleCard(Vehicle vehicle, User owner) {
        return VehicleCardDto.builder()
                .id(vehicle.getId())
                .nickname(vehicle.getNickname())
                .registrationNumber(vehicle.getRegistrationNumber())
                .manufacturer(vehicle.getManufacturer())
                .model(vehicle.getModel())
                .vehicleClass(vehicle.getVehicleClass())
                .fuelType(vehicle.getFuelType())
                .rto(vehicle.getRto())
                .owner(owner.getFullName())
                .registrationDate(vehicle.getRegistrationDate())
                .insuranceProvider(vehicle.getInsuranceProvider())
                .vehicleImageUrl(vehicle.getVehicleImageUrl())
                .vehicleStatus(vehicle.getVehicleStatus())
                .build();
    }

    private ComplianceStatusDto buildCompliance(Vehicle vehicle) {
        LocalDate today = LocalDate.now();
        // RC valid for 15 years from registration date
        LocalDate rcValidTill = vehicle.getRegistrationDate() != null
                ? LocalDate.parse(vehicle.getRegistrationDate()).plusYears(15)
                : null;

        return ComplianceStatusDto.builder()
                .rc(resolveStatus(rcValidTill, today))
                .puc(resolveStatus(vehicle.getPucValidTill(), today))
                .insurance(resolveStatus(vehicle.getInsuranceValidTill(), today))
                .tax(resolveStatus(vehicle.getTaxValidTill(), today))
                .permit(vehicle.getPermitValidTill() != null ? resolveStatus(vehicle.getPermitValidTill(), today)
                        : ComplianceItemDto.builder().status(ComplianceStatus.NOT_APPLICABLE).build())
                .fitness(vehicle.getFitnessValidTill() != null ? resolveStatus(vehicle.getFitnessValidTill(), today)
                        : ComplianceItemDto.builder().status(ComplianceStatus.NOT_APPLICABLE).build())
                .build();
    }

    private ComplianceItemDto resolveStatus(LocalDate validTill, LocalDate today) {
        if (validTill == null) {
            return ComplianceItemDto.builder().status(ComplianceStatus.NOT_APPLICABLE).build();
        }
        ComplianceStatus status;
        if (validTill.isBefore(today)) {
            status = ComplianceStatus.EXPIRED;
        } else if (validTill.isBefore(today.plusDays(30))) {
            status = ComplianceStatus.EXPIRING_SOON;
        } else {
            status = ComplianceStatus.VALID;
        }
        return ComplianceItemDto.builder().status(status).validTill(validTill).build();
    }

    private ChallanDto toChallanDto(Challan c) {
        return ChallanDto.builder()
                .id(c.getId())
                .offence(c.getOffence())
                .amount(c.getAmount())
                .challanDate(c.getChallanDate())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
