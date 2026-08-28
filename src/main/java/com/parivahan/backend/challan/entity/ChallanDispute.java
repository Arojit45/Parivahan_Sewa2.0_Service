package com.parivahan.backend.challan.entity;

import com.parivahan.backend.challan.enums.DisputeReason;
import com.parivahan.backend.challan.enums.DisputeStatus;
import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challan_disputes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChallanDispute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String disputeNumber;  // e.g. DISP-20250826-0001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challan_id", nullable = false)
    private Challan challan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeReason reason;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    // Comma-separated evidence file URLs (uploaded files)
    @Column(columnDefinition = "TEXT")
    private String evidenceUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.SUBMITTED;

    // Authority decision fields
    @Column(columnDefinition = "TEXT")
    private String authorityResponse;

    private LocalDateTime resolvedAt;
}
