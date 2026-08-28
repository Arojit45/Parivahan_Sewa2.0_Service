package com.parivahan.backend.vehicleregistration.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.vehicleregistration.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_registration_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehicleRegistrationApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true)
    private String applicationNumber;

    // --- Step 1 & 2: RTO Selection ---
    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String stateCode;

    @Column(length = 20)
    private String rtoCode;

    @Column(length = 200)
    private String rtoName;

    // --- Step 3: Eligibility & Details ---
    @Column(length = 50)
    private String vehicleCategory; // New, Used, Imported

    @Column(length = 50)
    private String usageType; // Private, Commercial

    @Column(length = 50)
    private String vehicleType; // Two Wheeler, Car, Transport

    private Boolean isEligible;

    // --- Step 4: Documents (Base64) ---
    @Lob
    @Column(columnDefinition = "TEXT")
    private String identityProof;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String addressProof;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String vehicleInvoice;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String insuranceProof;

    private Boolean documentsConfirmed;

    // --- Step 5: Fees ---
    private BigDecimal feeAmount;
    
    @Column(length = 50)
    @Builder.Default
    private String paymentStatus = "PENDING";
    
    @Column(length = 100)
    private String paymentTransactionId;

    private LocalDateTime paymentTimestamp;

    // --- Step 6: Appointment ---
    private LocalDate appointmentDate;

    @Column(length = 20)
    private String appointmentSlot;

    // --- Step 7 & 8: Application Status ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus applicationStatus = RegistrationStatus.DRAFT;

    @Column(length = 50)
    @Builder.Default
    private String inspectionStatus = "PENDING";

    // --- Progress Tracking ---
    @Column(nullable = false)
    @Builder.Default
    private Integer lastCompletedStep = 0;
}
