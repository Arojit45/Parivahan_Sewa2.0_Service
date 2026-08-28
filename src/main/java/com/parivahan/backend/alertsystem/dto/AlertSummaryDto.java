package com.parivahan.backend.alertsystem.dto;

import com.parivahan.backend.alertsystem.enums.AlertSeverity;
import com.parivahan.backend.alertsystem.enums.AlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Full alert detail returned to the frontend.
 * All fields are explicitly typed so the frontend knows exactly what to expect.
 */
@Data
@Builder
public class AlertSummaryDto {
    private Long id;
    private AlertType type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
