package com.parivahan.backend.vehicleregistration.service;

import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicleregistration.dto.*;
import com.parivahan.backend.vehicleregistration.entity.VehicleRegistrationApplication;
import com.parivahan.backend.vehicleregistration.enums.RegistrationStatus;
import com.parivahan.backend.vehicleregistration.repository.VehicleRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleRegistrationService {

    private final VehicleRegistrationRepository repository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public VrApplicationResponse createApplication(CreateVrRequest request) {
        User user = getCurrentUser();
        
        // Return existing draft if one exists, but update the state/stateCode first
        Optional<VehicleRegistrationApplication> existing = repository.findLatestDraftByUserId(user.getId());
        if (existing.isPresent()) {
            VehicleRegistrationApplication draft = existing.get();
            draft.setState(request.getState());
            draft.setStateCode(request.getStateCode());
            draft = repository.save(draft);
            return mapToResponse(draft);
        }

        VehicleRegistrationApplication app = VehicleRegistrationApplication.builder()
                .user(user)
                .state(request.getState())
                .stateCode(request.getStateCode())
                .applicationStatus(RegistrationStatus.DRAFT)
                .lastCompletedStep(1)
                .build();

        app = repository.save(app);
        return mapToResponse(app);
    }

    @Transactional
    public VrApplicationResponse updateApplicationStep(Long id, UpdateVrStepRequest request) {
        User user = getCurrentUser();
        VehicleRegistrationApplication app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Step 1: State
        if (request.getState() != null) {
            app.setState(request.getState());
            app.setStateCode(request.getStateCode());
        }

        // Step 2: RTO
        if (request.getRtoCode() != null) {
            app.setRtoCode(request.getRtoCode());
            app.setRtoName(request.getRtoName());
        }

        // Step 3: Eligibility
        if (request.getVehicleCategory() != null) {
            app.setVehicleCategory(request.getVehicleCategory());
            app.setUsageType(request.getUsageType());
            app.setVehicleType(request.getVehicleType());
            app.setIsEligible(request.getIsEligible());
        }

        // Step 4: Documents
        if (request.getDocumentsConfirmed() != null) {
            app.setIdentityProof(request.getIdentityProof());
            app.setAddressProof(request.getAddressProof());
            app.setVehicleInvoice(request.getVehicleInvoice());
            app.setInsuranceProof(request.getInsuranceProof());
            app.setDocumentsConfirmed(request.getDocumentsConfirmed());
        }

        // Step 5: Fees
        if (request.getFeeAmount() != null) {
            app.setFeeAmount(request.getFeeAmount());
        }

        // Step 6: Appointment
        if (request.getAppointmentDate() != null) {
            app.setAppointmentDate(request.getAppointmentDate());
            app.setAppointmentSlot(request.getAppointmentSlot());
        }

        if (request.getCompletedStep() != null) {
            app.setLastCompletedStep(Math.max(app.getLastCompletedStep(), request.getCompletedStep()));
        }

        app = repository.save(app);
        return mapToResponse(app);
    }

    @Transactional(readOnly = true)
    public Optional<VrApplicationResponse> getInProgressApplication() {
        User user = getCurrentUser();
        return repository.findLatestDraftByUserId(user.getId())
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<VrApplicationResponse> getMyApplications() {
        User user = getCurrentUser();
        return repository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VrApplicationResponse processMockPayment(MockVrPaymentRequest request) {
        User user = getCurrentUser();
        VehicleRegistrationApplication app = repository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if ("SUCCESS".equalsIgnoreCase(request.getSimulatedResult())) {
            app.setPaymentStatus("COMPLETED");
            app.setPaymentTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            app.setPaymentTimestamp(LocalDateTime.now());
            app.setApplicationStatus(RegistrationStatus.SUBMITTED);
            
            // Generate Application Number
            String appNum = "VR-" + (app.getStateCode() != null ? app.getStateCode() : "XX") + "-" + 
                           LocalDateTime.now().getYear() + "-" + 
                           String.format("%06d", (int)(Math.random() * 999999));
            app.setApplicationNumber(appNum);
            app.setLastCompletedStep(8);
        } else {
            app.setPaymentStatus("FAILED");
        }

        app = repository.save(app);
        return mapToResponse(app);
    }

    @Transactional(readOnly = true)
    public TrackVrApplicationResponse trackApplication(String applicationNumber) {
        VehicleRegistrationApplication app = repository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new RuntimeException("Application not found"));
                
        return TrackVrApplicationResponse.builder()
                .applicationNumber(app.getApplicationNumber())
                .applicantName(app.getUser().getFullName())
                .vehicleCategory(app.getVehicleCategory())
                .vehicleType(app.getVehicleType())
                .status(app.getApplicationStatus().name())
                .inspectionStatus(app.getInspectionStatus())
                .build();
    }

    private VrApplicationResponse mapToResponse(VehicleRegistrationApplication app) {
        return VrApplicationResponse.builder()
                .id(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .state(app.getState())
                .stateCode(app.getStateCode())
                .rtoCode(app.getRtoCode())
                .rtoName(app.getRtoName())
                .vehicleCategory(app.getVehicleCategory())
                .usageType(app.getUsageType())
                .vehicleType(app.getVehicleType())
                .isEligible(app.getIsEligible())
                .identityProof(app.getIdentityProof())
                .addressProof(app.getAddressProof())
                .vehicleInvoice(app.getVehicleInvoice())
                .insuranceProof(app.getInsuranceProof())
                .documentsConfirmed(app.getDocumentsConfirmed())
                .feeAmount(app.getFeeAmount())
                .paymentStatus(app.getPaymentStatus())
                .paymentTransactionId(app.getPaymentTransactionId())
                .paymentTimestamp(app.getPaymentTimestamp())
                .appointmentDate(app.getAppointmentDate())
                .appointmentSlot(app.getAppointmentSlot())
                .applicationStatus(app.getApplicationStatus().name())
                .inspectionStatus(app.getInspectionStatus())
                .lastCompletedStep(app.getLastCompletedStep())
                .build();
    }
}
