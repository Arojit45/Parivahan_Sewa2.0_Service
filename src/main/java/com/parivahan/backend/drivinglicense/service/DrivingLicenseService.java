package com.parivahan.backend.drivinglicense.service;

import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.drivinglicense.dto.*;
import com.parivahan.backend.drivinglicense.entity.DrivingLicenseApplication;
import com.parivahan.backend.drivinglicense.entity.DrivingSchool;
import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import com.parivahan.backend.drivinglicense.enums.PaymentStatus;
import com.parivahan.backend.drivinglicense.repository.DrivingLicenseApplicationRepository;
import com.parivahan.backend.drivinglicense.repository.DrivingSchoolRepository;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrivingLicenseService {

    private final DrivingLicenseApplicationRepository applicationRepository;
    private final DrivingSchoolRepository schoolRepository;
    private final UserRepository userRepository;

    /** Fee map by vehicle class */
    private static final Map<String, BigDecimal> FEE_BY_CLASS = Map.of(
            "LMV",   new BigDecimal("700"),
            "MCWG",  new BigDecimal("500"),
            "MCWOG", new BigDecimal("500"),
            "HMV",   new BigDecimal("1000"),
            "HPMV",  new BigDecimal("1000"),
            "TRANS", new BigDecimal("800"),
            "HTV",   new BigDecimal("1000")
    );

    // -----------------------------------------------------------------------
    // Create (Step 1 — initial)
    // -----------------------------------------------------------------------

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest req) {
        User user = getCurrentUser();

        // Reject if user already has an active in-progress application
        List<ApplicationStatus> terminalStatuses = List.of(
                ApplicationStatus.DL_DISPATCHED,
                ApplicationStatus.REJECTED
        );
        Optional<DrivingLicenseApplication> existing = applicationRepository
                .findTopByUserIdAndApplicationStatusNotInOrderByCreatedAtDesc(user.getId(), terminalStatuses);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        DrivingLicenseApplication app = DrivingLicenseApplication.builder()
                .user(user)
                .state(req.getState())
                .stateCode(req.getStateCode())
                .lastCompletedStep(1)
                .build();

        return toResponse(applicationRepository.save(app));
    }

    // -----------------------------------------------------------------------
    // Update step
    // -----------------------------------------------------------------------

    @Transactional
    public ApplicationResponse updateApplicationStep(Long appId, UpdateApplicationStepRequest req) {
        User user = getCurrentUser();
        DrivingLicenseApplication app = getOwnedApplication(appId, user);

        int step = req.getCompletedStep();

        switch (step) {
            case 1 -> {
                if (req.getState() != null) app.setState(req.getState());
                if (req.getStateCode() != null) app.setStateCode(req.getStateCode());
            }
            case 2 -> {
                if (req.getRtoCode() != null) app.setRtoCode(req.getRtoCode());
                if (req.getRtoName() != null) app.setRtoName(req.getRtoName());
            }
            case 3 -> {
                if (req.getVehicleClass() != null) {
                    app.setVehicleClass(req.getVehicleClass());
                    // Auto-calculate fee
                    app.setFeeAmount(FEE_BY_CLASS.getOrDefault(req.getVehicleClass(), new BigDecimal("700")));
                }
            }
            case 4 -> {
                if (req.getHasLL() != null) app.setHasLL(req.getHasLL());
                if (req.getLlNumber() != null) app.setLlNumber(req.getLlNumber());
            }
            case 5 -> {
                if (req.getApplicantName() != null) app.setApplicantName(req.getApplicantName());
                if (req.getDob() != null) app.setDob(req.getDob());
                if (req.getAddress() != null) app.setAddress(req.getAddress());
                if (req.getAadharNumber() != null) app.setAadharNumber(req.getAadharNumber());
                if (req.getIsEligible() != null) app.setIsEligible(req.getIsEligible());
            }
            case 6 -> {
                if (req.getDocumentsConfirmed() != null) app.setDocumentsConfirmed(req.getDocumentsConfirmed());
                if (req.getSelectedDrivingSchoolId() != null) {
                    DrivingSchool school = schoolRepository.findById(req.getSelectedDrivingSchoolId())
                            .orElseThrow(() -> new ResourceNotFoundException("Driving school not found"));
                    app.setSelectedDrivingSchool(school);
                }
            }
            case 7 -> {
                if (req.getAppointmentDate() != null) app.setAppointmentDate(req.getAppointmentDate());
                if (req.getAppointmentSlot() != null) app.setAppointmentSlot(req.getAppointmentSlot());
                app.setApplicationStatus(ApplicationStatus.TEST_SCHEDULED);
            }
        }

        // Always update progress tracker
        if (step > app.getLastCompletedStep()) {
            app.setLastCompletedStep(step);
        }

        return toResponse(applicationRepository.save(app));
    }

    // -----------------------------------------------------------------------
    // Get in-progress application (for resume)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<ApplicationResponse> getInProgressApplication() {
        User user = getCurrentUser();
        List<ApplicationStatus> terminalStatuses = List.of(
                ApplicationStatus.DL_DISPATCHED,
                ApplicationStatus.REJECTED
        );
        return applicationRepository
                .findTopByUserIdAndApplicationStatusNotInOrderByCreatedAtDesc(user.getId(), terminalStatuses)
                .map(this::toResponse);
    }

    // -----------------------------------------------------------------------
    // Get all user applications
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications() {
        User user = getCurrentUser();
        return applicationRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -----------------------------------------------------------------------
    // Mock Payment
    // -----------------------------------------------------------------------

    @Transactional
    public ApplicationResponse processMockPayment(MockPaymentRequest req) {
        User user = getCurrentUser();
        DrivingLicenseApplication app = getOwnedApplication(req.getApplicationId(), user);

        if (app.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment has already been completed for this application");
        }

        String txnId = "MOCK-DL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("SUCCESS".equalsIgnoreCase(req.getSimulatedResult())) {
            app.setPaymentStatus(PaymentStatus.COMPLETED);
            app.setPaymentTransactionId(txnId);
            app.setPaymentTimestamp(LocalDateTime.now());
            app.setApplicationStatus(ApplicationStatus.SUBMITTED);
            app.setLastCompletedStep(8);

            // Generate unique application number: DL-{STATECODE}-{YEAR}-{6 random digits}
            String appNumber = "DL-" +
                    (app.getStateCode() != null ? app.getStateCode() : "XX") + "-" +
                    LocalDateTime.now().getYear() + "-" +
                    String.format("%06d", (int) (Math.random() * 999999));
            app.setApplicationNumber(appNumber);
        } else {
            app.setPaymentStatus(PaymentStatus.FAILED);
            app.setPaymentTransactionId(txnId);
        }

        return toResponse(applicationRepository.save(app));
    }

    // -----------------------------------------------------------------------
    // Public: Track by application number
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TrackApplicationResponse trackApplication(String applicationNumber) {
        DrivingLicenseApplication app = applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with number: " + applicationNumber));

        return TrackApplicationResponse.builder()
                .applicationNumber(app.getApplicationNumber())
                .applicantName(app.getApplicantName())
                .state(app.getState())
                .rtoName(app.getRtoName())
                .vehicleClass(app.getVehicleClass())
                .applicationStatus(app.getApplicationStatus())
                .testResult(app.getTestResult())
                .appointmentDate(app.getAppointmentDate())
                .appointmentSlot(app.getAppointmentSlot())
                .submittedAt(app.getUpdatedAt())
                .updatedAt(app.getUpdatedAt())
                .statusMessage(resolveStatusMessage(app.getApplicationStatus()))
                .build();
    }

    // -----------------------------------------------------------------------
    // Driving Schools
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DrivingSchoolResponse> searchSchools(String state, String city) {
        List<DrivingSchool> schools = (city != null && !city.isBlank())
                ? schoolRepository.findByStateIgnoreCaseAndCityIgnoreCase(state, city)
                : schoolRepository.findByStateIgnoreCase(state);

        return schools.stream().map(this::toSchoolResponse).toList();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DrivingLicenseApplication getOwnedApplication(Long appId, User user) {
        DrivingLicenseApplication app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!app.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: You do not own this application");
        }
        return app;
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String resolveStatusMessage(ApplicationStatus status) {
        return switch (status) {
            case DRAFT -> "Your application is in progress.";
            case SUBMITTED -> "Your application has been submitted and is under review.";
            case UNDER_REVIEW -> "Your application is currently under review by the RTO.";
            case TEST_SCHEDULED -> "Your driving test has been scheduled. Please be on time!";
            case PASS -> "Congratulations! You have passed your driving test. Your DL is being processed.";
            case FAIL -> "You did not pass the driving test. You may apply for a re-test.";
            case DL_DISPATCHED -> "Your Driving Licence has been dispatched. It will arrive within 7-10 working days.";
            case REJECTED -> "Your application was rejected. Please contact your RTO for more details.";
        };
    }

    private ApplicationResponse toResponse(DrivingLicenseApplication app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .state(app.getState())
                .stateCode(app.getStateCode())
                .rtoCode(app.getRtoCode())
                .rtoName(app.getRtoName())
                .vehicleClass(app.getVehicleClass())
                .hasLL(app.getHasLL())
                .llNumber(app.getLlNumber())
                .applicantName(app.getApplicantName())
                .dob(app.getDob())
                .address(app.getAddress())
                .aadharNumber(app.getAadharNumber())
                .isEligible(app.getIsEligible())
                .documentsConfirmed(app.getDocumentsConfirmed())
                .selectedDrivingSchoolId(app.getSelectedDrivingSchool() != null ? app.getSelectedDrivingSchool().getId() : null)
                .selectedDrivingSchoolName(app.getSelectedDrivingSchool() != null ? app.getSelectedDrivingSchool().getName() : null)
                .appointmentDate(app.getAppointmentDate())
                .appointmentSlot(app.getAppointmentSlot())
                .feeAmount(app.getFeeAmount())
                .paymentStatus(app.getPaymentStatus())
                .paymentTransactionId(app.getPaymentTransactionId())
                .paymentTimestamp(app.getPaymentTimestamp())
                .applicationStatus(app.getApplicationStatus())
                .testResult(app.getTestResult())
                .lastCompletedStep(app.getLastCompletedStep())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    private DrivingSchoolResponse toSchoolResponse(DrivingSchool s) {
        return DrivingSchoolResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .state(s.getState())
                .city(s.getCity())
                .pincode(s.getPincode())
                .phone(s.getPhone())
                .rating(s.getRating())
                .isGovernmentApproved(s.getIsGovernmentApproved())
                .licenseNumber(s.getLicenseNumber())
                .build();
    }
}
