package com.parivahan.backend.correction.service;

import com.parivahan.backend.correction.dto.CorrectionResponse;
import com.parivahan.backend.correction.dto.SubmitCorrectionRequest;
import com.parivahan.backend.correction.entity.CorrectionRequest;
import com.parivahan.backend.correction.enums.CorrectionStatus;
import com.parivahan.backend.correction.repository.CorrectionRepository;
import com.parivahan.backend.drivinglicense.entity.DrivingLicenseApplication;
import com.parivahan.backend.drivinglicense.repository.DrivingLicenseApplicationRepository;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.domain.Vehicle;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import com.parivahan.backend.vehicleregistration.entity.VehicleRegistrationApplication;
import com.parivahan.backend.vehicleregistration.repository.VehicleRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleRegistrationRepository vrRepository;
    private final DrivingLicenseApplicationRepository dlRepository;

    @Transactional
    public CorrectionResponse submitCorrection(SubmitCorrectionRequest request) {
        User user = getCurrentUser();

        // Security check: Verify user owns the target
        verifyOwnership(user.getId(), request);

        CorrectionRequest correction = CorrectionRequest.builder()
                .user(user)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .fieldName(request.getFieldName())
                .currentValue(request.getCurrentValue())
                .requestedValue(request.getRequestedValue())
                .reason(request.getReason())
                .evidenceBase64(request.getEvidenceBase64())
                .status(CorrectionStatus.SUBMITTED)
                .build();

        return mapToResponse(correctionRepository.save(correction));
    }

    @Transactional(readOnly = true)
    public List<CorrectionResponse> getMyCorrections() {
        User user = getCurrentUser();
        return correctionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CorrectionResponse approveCorrection(Long id) {
        CorrectionRequest correction = correctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Correction not found"));

        if (correction.getStatus() == CorrectionStatus.APPROVED) {
            return mapToResponse(correction);
        }

        // Apply correction to the official source of truth
        applyCorrectionToTarget(correction);

        correction.setStatus(CorrectionStatus.APPROVED);
        return mapToResponse(correctionRepository.save(correction));
    }

    private void applyCorrectionToTarget(CorrectionRequest correction) {
        Object targetEntity;

        switch (correction.getTargetType()) {
            case VEHICLE:
                targetEntity = vehicleRepository.findById(correction.getTargetId())
                        .orElseThrow(() -> new RuntimeException("Vehicle not found"));
                break;
            case VEHICLE_REGISTRATION_APPLICATION:
                targetEntity = vrRepository.findById(correction.getTargetId())
                        .orElseThrow(() -> new RuntimeException("VR App not found"));
                break;
            case DRIVING_LICENSE_APPLICATION:
                targetEntity = dlRepository.findById(correction.getTargetId())
                        .orElseThrow(() -> new RuntimeException("DL App not found"));
                break;
            default:
                throw new IllegalArgumentException("Invalid target type");
        }

        Field field = ReflectionUtils.findField(targetEntity.getClass(), correction.getFieldName());
        if (field == null) {
            throw new IllegalArgumentException("Field " + correction.getFieldName() + " does not exist on " + correction.getTargetType());
        }

        field.setAccessible(true);
        // Basic type conversion, usually requestedValue is String, but field could be Boolean, Integer, etc.
        // For simplicity, we handle String. If other types are needed, conversion logic belongs here.
        ReflectionUtils.setField(field, targetEntity, correction.getRequestedValue());

        // Save the updated entity
        switch (correction.getTargetType()) {
            case VEHICLE:
                vehicleRepository.save((Vehicle) targetEntity);
                break;
            case VEHICLE_REGISTRATION_APPLICATION:
                vrRepository.save((VehicleRegistrationApplication) targetEntity);
                break;
            case DRIVING_LICENSE_APPLICATION:
                dlRepository.save((DrivingLicenseApplication) targetEntity);
                break;
        }
    }

    private void verifyOwnership(Long userId, SubmitCorrectionRequest request) {
        boolean isOwner = false;
        switch (request.getTargetType()) {
            case VEHICLE:
                isOwner = vehicleRepository.findById(request.getTargetId())
                        .map(v -> v.getUser().getId().equals(userId)).orElse(false);
                break;
            case VEHICLE_REGISTRATION_APPLICATION:
                isOwner = vrRepository.findById(request.getTargetId())
                        .map(vr -> vr.getUser().getId().equals(userId)).orElse(false);
                break;
            case DRIVING_LICENSE_APPLICATION:
                isOwner = dlRepository.findById(request.getTargetId())
                        .map(dl -> dl.getUser().getId().equals(userId)).orElse(false);
                break;
        }
        if (!isOwner) {
            throw new com.parivahan.backend.common.exception.ResourceNotFoundException("Unauthorized: You do not own this target or it does not exist.");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CorrectionResponse mapToResponse(CorrectionRequest correction) {
        return CorrectionResponse.builder()
                .id(correction.getId())
                .targetType(correction.getTargetType())
                .targetId(correction.getTargetId())
                .fieldName(correction.getFieldName())
                .currentValue(correction.getCurrentValue())
                .requestedValue(correction.getRequestedValue())
                .reason(correction.getReason())
                .status(correction.getStatus())
                .rejectionReason(correction.getRejectionReason())
                .createdAt(correction.getCreatedAt())
                .updatedAt(correction.getUpdatedAt())
                .build();
    }
}
