package com.parivahan.backend.guardianmode.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A single geofence breach event — shown on the vehicle timeline and live map.
 */
@Data
@Builder
public class GeofenceBreachEventDto {
    private Long id;
    private Long vehicleId;
    private String registrationNumber;

    // Breach location — frontend uses these to pin on the live map
    private Double breachLat;
    private Double breachLng;

    private Double distanceFromSafeZone;  // meters
    private String lastKnownAddress;
    private LocalDateTime breachedAt;
}
