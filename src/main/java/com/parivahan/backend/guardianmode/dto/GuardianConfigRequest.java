package com.parivahan.backend.guardianmode.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class GuardianConfigRequest {

    // Either provide lat/lng OR an area name — service resolves area name to coordinates
    private Double safeLat;
    private Double safeLng;
    private String safeAreaName;

    @Min(value = 500, message = "Minimum radius is 500 meters")
    @Max(value = 50000, message = "Maximum radius is 50 km")
    private Double radiusMeters;

    private Boolean pushAlertsEnabled;
    private Boolean smsAlertsEnabled;
    private Boolean emailAlertsEnabled;
    private Boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
}
