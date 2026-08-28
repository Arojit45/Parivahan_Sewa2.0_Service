package com.parivahan.backend.guardianmode.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Full Guardian Mode config returned to the frontend.
 */
@Data
@Builder
public class GuardianConfigDto {
    private Long vehicleId;
    private String registrationNumber;
    private boolean enabled;
    private Double safeLat;
    private Double safeLng;
    private String safeAreaName;
    private Double radiusMeters;
    private boolean pushAlertsEnabled;
    private boolean smsAlertsEnabled;
    private boolean emailAlertsEnabled;
    private boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
    private LocalDateTime lastBreachAt;
}
