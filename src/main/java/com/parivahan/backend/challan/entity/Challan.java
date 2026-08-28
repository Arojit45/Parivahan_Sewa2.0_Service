package com.parivahan.backend.challan.entity;

import com.parivahan.backend.challan.enums.ChallanStatus;
import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "challans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Challan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private String offence;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDate challanDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChallanStatus status = ChallanStatus.PENDING;

    // Set when paid
    private LocalDate paymentDate;
    private String transactionId;
}
