package com.parivahan.backend.guardianmode.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "geofence_breach_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeofenceBreachEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Where the vehicle was when the breach was detected
    private Double breachLat;
    private Double breachLng;

    // Meters outside the safe zone
    private Double distanceFromSafeZone;

    // Address at time of breach (from live location mock)
    private String lastKnownAddress;

    private LocalDateTime breachedAt;
}
