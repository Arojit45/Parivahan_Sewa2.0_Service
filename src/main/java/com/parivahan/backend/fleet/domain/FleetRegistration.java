package com.parivahan.backend.fleet.domain;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.fleet.enums.FleetStatus;
import com.parivahan.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fleet_registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String fleetName;

    @Column(unique = true)
    private String fleetRegistrationNumber; // e.g. FLT-2026-001245, set on approval

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FleetStatus status = FleetStatus.PENDING;

    private String rejectionReason;

    // Vehicle registration number entered during registration
    @Column(nullable = false)
    private String vehicleRegistrationNumber;

    // Documents stored as Base64 (same pattern as corrections module)
    @Column(columnDefinition = "TEXT")
    private String document1Base64;

    @Column(columnDefinition = "TEXT")
    private String document2Base64;

    @Column(columnDefinition = "TEXT")
    private String businessProofBase64;
}
