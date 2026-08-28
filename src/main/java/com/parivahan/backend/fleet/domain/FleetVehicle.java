package com.parivahan.backend.fleet.domain;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_vehicles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fleet_id", "vehicle_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetVehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private FleetRegistration fleet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime addedAt;
}
