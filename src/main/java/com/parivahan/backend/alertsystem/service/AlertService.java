package com.parivahan.backend.alertsystem.service;

import com.parivahan.backend.alertsystem.dto.AlertSummaryDto;
import com.parivahan.backend.alertsystem.entity.Alert;
import com.parivahan.backend.alertsystem.enums.AlertSeverity;
import com.parivahan.backend.alertsystem.enums.AlertType;
import com.parivahan.backend.alertsystem.repository.AlertRepository;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final TwilioSmsService twilioSmsService;

    /**
     * Creates and dispatches an alert to all specified channels.
     *
     * @param user        the target user
     * @param type        alert type
     * @param severity    alert severity
     * @param title       short title
     * @param message     full message
     * @param referenceId ID of the related entity (vehicleId, challanId, etc.)
     * @param sendSms     whether to also send an SMS to the user's mobile
     */
    @Transactional
    public Alert send(User user, AlertType type, AlertSeverity severity,
                      String title, String message, Long referenceId, boolean sendSms) {

        String channels = sendSms ? "IN_APP,SMS" : "IN_APP";

        Alert alert = Alert.builder()
                .user(user)
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .channels(channels)
                .read(false)
                .build();

        alert = alertRepository.save(alert);

        if (sendSms) {
            twilioSmsService.sendSms(user.getMobileNumber(),
                    "[Parivahan] " + title + ": " + message);
        }

        log.info("Alert dispatched [{}][{}] → userId={}", severity, type, user.getId());
        return alert;
    }

    // -----------------------------------------------------------------------
    // Frontend-facing methods — all ownership-enforced
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AlertSummaryDto> getAllAlerts() {
        User user = getCurrentUser();
        return alertRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCount() {
        User user = getCurrentUser();
        return Map.of("unreadCount", alertRepository.countByUserIdAndReadFalse(user.getId()));
    }

    @Transactional
    public AlertSummaryDto markRead(Long alertId) {
        Alert alert = getOwnedAlert(alertId);
        alert.setRead(true);
        return toDto(alertRepository.save(alert));
    }

    @Transactional
    public void markAllRead() {
        User user = getCurrentUser();
        alertRepository.markAllReadByUserId(user.getId());
    }

    @Transactional
    public void dismiss(Long alertId) {
        Alert alert = getOwnedAlert(alertId);
        alertRepository.delete(alert);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Alert getOwnedAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        if (!alert.getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Access denied");
        }
        return alert;
    }

    private AlertSummaryDto toDto(Alert a) {
        return AlertSummaryDto.builder()
                .id(a.getId())
                .type(a.getType())
                .severity(a.getSeverity())
                .title(a.getTitle())
                .message(a.getMessage())
                .referenceId(a.getReferenceId())
                .read(a.isRead())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
