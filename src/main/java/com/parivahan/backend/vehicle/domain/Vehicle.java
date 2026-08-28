package com.parivahan.backend.vehicle.domain;

import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    private String nickname;
    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String registrationDate;
    private String rto;
    private String insuranceProvider;
    private String vehicleImageUrl;

    // Compliance dates
    private LocalDate insuranceValidTill;
    private LocalDate pucValidTill;
    private LocalDate taxValidTill;
    private LocalDate permitValidTill;   // null = Not Applicable
    private LocalDate fitnessValidTill;  // null = Not Applicable

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus vehicleStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Challan> challans;
}
