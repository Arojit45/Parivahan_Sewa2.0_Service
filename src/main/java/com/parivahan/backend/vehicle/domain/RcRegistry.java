package com.parivahan.backend.vehicle.domain;

import com.parivahan.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rc_registry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RcRegistry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerMobile;

    private String manufacturer;
    private String model;
    private String vehicleClass;
    private String fuelType;
    private String registrationDate;
    private String rto;
}
