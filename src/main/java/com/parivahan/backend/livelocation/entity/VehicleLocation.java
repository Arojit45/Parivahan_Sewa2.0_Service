package com.parivahan.backend.livelocation.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehicleLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Vehicle vehicle;

    private Double latitude;
    private Double longitude;
    private Double speed;       // km/h
    private String heading;     // e.g., "North", "South-East"
    private String address;     // Reverse-geocoded address string (mock)

    private LocalDateTime lastUpdated;
}
