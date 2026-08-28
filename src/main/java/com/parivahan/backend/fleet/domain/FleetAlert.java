package com.parivahan.backend.fleet.domain;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.fleet.enums.FleetAlertType;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private FleetRegistration fleet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetAlertType alertType;

    @Column(columnDefinition = "TEXT")
    private String message;

    // OPEN or RESOLVED
    @Builder.Default
    private String status = "OPEN";

    // Used for cooldown: don't re-trigger alert if within cooldown window
    private LocalDateTime lastTriggeredAt;

    private LocalDateTime resolvedAt;
}
