package com.parivahan.backend.drivinglicense.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import com.parivahan.backend.drivinglicense.enums.PaymentStatus;
import com.parivahan.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "driving_license_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DrivingLicenseApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Auto-generated after successful payment
    @Column(unique = true)
    private String applicationNumber;

    // --- Step 1: State ---
    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String stateCode;

    // --- Step 2: RTO ---
    @Column(length = 20)
    private String rtoCode;

    @Column(length = 200)
    private String rtoName;

    // --- Step 3: Vehicle Class ---
    @Column(length = 20)
    private String vehicleClass;

    // --- Step 4: Learner's Licence ---
    private Boolean hasLL;

    @Column(length = 50)
    private String llNumber;

    // --- Step 5: Eligibility / Applicant Details ---
    @Column(length = 150)
    private String applicantName;

    private LocalDate dob;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 12)
    private String aadharNumber;

    private Boolean isEligible;

    // --- Step 6: Documents & School ---
    private Boolean documentsConfirmed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_school_id")
    private DrivingSchool selectedDrivingSchool;

    // --- Step 7: Appointment ---
    private LocalDate appointmentDate;

    @Column(length = 20)
    private String appointmentSlot;

    // --- Step 8: Payment ---
    private BigDecimal feeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 100)
    private String paymentTransactionId;

    private LocalDateTime paymentTimestamp;

    // --- Step 9 / Admin: Status ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.DRAFT;

    @Column(length = 20)
    @Builder.Default
    private String testResult = "PENDING";

    // --- Progress Tracking ---
    @Column(nullable = false)
    @Builder.Default
    private Integer lastCompletedStep = 0;
}
