package com.parivahan.backend.fleet.domain;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private FleetRegistration fleet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private String startLocation;

    @Column(nullable = false)
    private String destination;

    // Optional start lat/lng for deviation check
    private Double startLat;
    private Double startLng;
    private Double destLat;
    private Double destLng;

    // Corridor tolerance in meters
    @Builder.Default
    private Integer toleranceMeters = 500;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FleetRouteStatus routeStatus = FleetRouteStatus.ACTIVE;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
