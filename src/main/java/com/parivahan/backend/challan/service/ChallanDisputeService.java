package com.parivahan.backend.challan.service;

import com.parivahan.backend.challan.dto.DisputeDetailDto;
import com.parivahan.backend.challan.dto.DisputeRequestDto;
import com.parivahan.backend.challan.dto.DisputeTimelineEventDto;
import com.parivahan.backend.challan.dto.DisputeTimelineEventDto.StepStatus;
import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.challan.entity.ChallanDispute;
import com.parivahan.backend.challan.enums.ChallanStatus;
import com.parivahan.backend.challan.enums.DisputeStatus;
import com.parivahan.backend.challan.repository.ChallanDisputeRepository;
import com.parivahan.backend.challan.repository.ChallanRepository;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallanDisputeService {

    private final ChallanRepository challanRepository;
    private final ChallanDisputeRepository disputeRepository;
    private final UserRepository userRepository;
    private final MockAuthorityDecisionEngine authorityEngine;

    private static final DateTimeFormatter DISPUTE_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Raise a new dispute on a challan. */
    @Transactional
    public DisputeDetailDto raiseDispute(Long challanId, DisputeRequestDto request) {
        Challan challan = getOwnedChallan(challanId);
        User user = getCurrentUser();

        if (challan.getStatus() == ChallanStatus.PAID) {
            throw new IllegalStateException("Cannot dispute a challan that has already been paid.");
        }
        if (disputeRepository.findByChallanId(challanId).isPresent()) {
            throw new IllegalStateException("A dispute for this challan is already active.");
        }

        String disputeNumber = "DISP-" + LocalDateTime.now().format(DISPUTE_NUMBER_FORMAT) + "-"
                + String.format("%04d", (long) (Math.random() * 9000) + 1000);

        ChallanDispute dispute = ChallanDispute.builder()
                .disputeNumber(disputeNumber)
                .challan(challan)
                .user(user)
                .reason(request.getReason())
                .explanation(request.getExplanation())
                .evidenceUrls(request.getEvidenceUrls())
                .status(DisputeStatus.SUBMITTED)
                .build();

        dispute = disputeRepository.save(dispute);

        // Move challan to DISPUTED status
        challan.setStatus(ChallanStatus.DISPUTED);
        challanRepository.save(challan);

        return toDetailDto(dispute);
    }

    /** Get dispute for a specific challan. */
    @Transactional(readOnly = true)
    public DisputeDetailDto getDisputeByChallanId(Long challanId) {
        getOwnedChallan(challanId); // ownership check
        ChallanDispute dispute = disputeRepository.findByChallanId(challanId)
                .orElseThrow(() -> new ResourceNotFoundException("No dispute found for this challan"));
        return toDetailDto(dispute);
    }

    /** All disputes for the current user. */
    @Transactional(readOnly = true)
    public List<DisputeDetailDto> getAllDisputesForCurrentUser() {
        User user = getCurrentUser();
        return disputeRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::toDetailDto)
                .collect(Collectors.toList());
    }

    /** Timeline for a specific dispute. */
    @Transactional(readOnly = true)
    public List<DisputeTimelineEventDto> getTimeline(Long disputeId) {
        ChallanDispute dispute = getOwnedDispute(disputeId);
        return buildTimeline(dispute);
    }

    /**
     * Simulated authority decision. Randomly upholds or cancels the challan.
     * Future: replace body with real government grievance API call.
     */
    @Transactional
    public DisputeDetailDto processAuthorityDecision(Long disputeId) {
        ChallanDispute dispute = getOwnedDispute(disputeId);

        if (dispute.getStatus() == DisputeStatus.RESOLVED
                || dispute.getStatus() == DisputeStatus.UPHELD
                || dispute.getStatus() == DisputeStatus.CANCELLED) {
            throw new IllegalStateException("This dispute has already been resolved.");
        }

        DisputeStatus decision = authorityEngine.decide();
        String message = authorityEngine.getDecisionMessage(decision);

        dispute.setStatus(decision);
        dispute.setAuthorityResponse(message);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        // Update challan status accordingly
        Challan challan = dispute.getChallan();
        if (decision == DisputeStatus.CANCELLED) {
            // Challan is voided — set back to PENDING so user sees it's cancelled
            challan.setStatus(ChallanStatus.PAID); // treat as resolved/no payment needed
        }
        challanRepository.save(challan);

        return toDetailDto(dispute);
    }

    // -----------------------------------------------------------------------
    // Timeline Builder
    // -----------------------------------------------------------------------

    private List<DisputeTimelineEventDto> buildTimeline(ChallanDispute dispute) {
        List<DisputeTimelineEventDto> timeline = new ArrayList<>();
        DisputeStatus current = dispute.getStatus();

        // Ordered timeline steps
        record Step(int num, String label, DisputeStatus triggeredBy, String description) {}

        List<Step> steps = List.of(
                new Step(1, "Submitted", DisputeStatus.SUBMITTED, "Your dispute has been registered."),
                new Step(2, "Under Review", DisputeStatus.UNDER_REVIEW, "The authority is reviewing your case."),
                new Step(3, "Authority Decision", DisputeStatus.UPHELD, "The authority has made a decision."),
                new Step(4, "Resolved", DisputeStatus.RESOLVED, "The dispute process is complete.")
        );

        int currentOrder = getStatusOrder(current);

        for (Step step : steps) {
            int stepOrder = step.num();
            StepStatus status;
            if (stepOrder < currentOrder) {
                status = StepStatus.DONE;
            } else if (stepOrder == currentOrder) {
                status = StepStatus.ACTIVE;
            } else {
                status = StepStatus.PENDING;
            }

            timeline.add(DisputeTimelineEventDto.builder()
                    .step(step.num())
                    .label(step.label())
                    .stepStatus(status)
                    .date(stepOrder <= currentOrder ? dispute.getCreatedAt() : null)
                    .description(step.description())
                    .build());
        }

        return timeline;
    }

    private int getStatusOrder(DisputeStatus status) {
        return switch (status) {
            case SUBMITTED -> 1;
            case UNDER_REVIEW, ACTION_REQUIRED -> 2;
            case UPHELD, CANCELLED -> 3;
            case RESOLVED -> 4;
        };
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DisputeDetailDto toDetailDto(ChallanDispute d) {
        return DisputeDetailDto.builder()
                .id(d.getId())
                .disputeNumber(d.getDisputeNumber())
                .challanId(d.getChallan().getId())
                .registrationNumber(d.getChallan().getVehicle().getRegistrationNumber())
                .offence(d.getChallan().getOffence())
                .reason(d.getReason())
                .explanation(d.getExplanation())
                .evidenceUrls(d.getEvidenceUrls())
                .status(d.getStatus())
                .authorityResponse(d.getAuthorityResponse())
                .submittedAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
                .timeline(buildTimeline(d))
                .build();
    }

    private Challan getOwnedChallan(Long challanId) {
        Challan challan = challanRepository.findById(challanId)
                .orElseThrow(() -> new ResourceNotFoundException("Challan not found"));
        if (!challan.getVehicle().getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied");
        }
        return challan;
    }

    private ChallanDispute getOwnedDispute(Long disputeId) {
        ChallanDispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));
        if (!dispute.getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied");
        }
        return dispute;
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
