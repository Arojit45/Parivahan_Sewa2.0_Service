package com.parivahan.backend.guardianmode.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guardian_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GuardianConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private boolean enabled = false;

    // Safe zone center coordinates
    private Double safeLat;
    private Double safeLng;

    // Optional: area name provided by user (e.g. "Connaught Place, Delhi")
    private String safeAreaName;

    // Geofence radius in meters (e.g. 2000 = 2km)
    @Builder.Default
    private Double radiusMeters = 2000.0;

    @Builder.Default
    private boolean pushAlertsEnabled = true;

    @Builder.Default
    private boolean smsAlertsEnabled = true;

    @Builder.Default
    private boolean emailAlertsEnabled = true;

    @Builder.Default
    private boolean quietHoursEnabled = true;

    @Builder.Default
    private String quietHoursStart = "22:00";

    @Builder.Default
    private String quietHoursEnd = "06:00";

    // Timestamp of last recorded geofence breach
    private LocalDateTime lastBreachAt;
}
