package com.parivahan.backend.mydocuments.service;

import com.parivahan.backend.drivinglicense.dto.ApplicationResponse;
import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import com.parivahan.backend.drivinglicense.repository.DrivingLicenseApplicationRepository;
import com.parivahan.backend.mydocuments.dto.MyDocumentsResponse;
import com.parivahan.backend.mydocuments.dto.MyVehicleDocumentDto;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import com.parivahan.backend.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyDocumentsService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DrivingLicenseApplicationRepository dlRepository;

    public MyDocumentsResponse getMyDocuments() {
        User user = getCurrentUser();

        // 1. Fetch user's vehicles
        List<MyVehicleDocumentDto> vehicles = vehicleRepository.findByUserId(user.getId())
                .stream()
                .map(v -> MyVehicleDocumentDto.builder()
                        .id(v.getId())
                        .registrationNumber(v.getRegistrationNumber())
                        .nickname(v.getNickname())
                        .manufacturer(v.getManufacturer())
                        .model(v.getModel())
                        .vehicleClass(v.getVehicleClass())
                        .fuelType(v.getFuelType())
                        .registrationDate(v.getRegistrationDate())
                        .rto(v.getRto())
                        .insuranceProvider(v.getInsuranceProvider())
                        .vehicleImageUrl(v.getVehicleImageUrl())
                        .insuranceValidTill(v.getInsuranceValidTill())
                        .pucValidTill(v.getPucValidTill())
                        .taxValidTill(v.getTaxValidTill())
                        .permitValidTill(v.getPermitValidTill())
                        .fitnessValidTill(v.getFitnessValidTill())
                        .vehicleStatus(v.getVehicleStatus())
                        .build())
                .collect(Collectors.toList());

        // 2. Fetch user's Driving License (completed application)
        ApplicationResponse dl = dlRepository.findByUserId(user.getId())
                .stream()
                .filter(app -> app.getApplicationStatus() == ApplicationStatus.DL_DISPATCHED || app.getApplicationStatus() == ApplicationStatus.PASS)
                .max(Comparator.comparing(app -> app.getCreatedAt() != null ? app.getCreatedAt() : java.time.LocalDateTime.MIN))
                .map(app -> ApplicationResponse.builder()
                        .id(app.getId())
                        .applicationNumber(app.getApplicationNumber())
                        .state(app.getState())
                        .rtoName(app.getRtoName())
                        .vehicleClass(app.getVehicleClass())
                        .applicantName(app.getApplicantName())
                        .dob(app.getDob())
                        .address(app.getAddress())
                        .applicationStatus(app.getApplicationStatus())
                        .build())
                .orElse(null);

        return MyDocumentsResponse.builder()
                .vehicles(vehicles)
                .drivingLicense(dl)
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
