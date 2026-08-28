package com.parivahan.backend.correction.entity;

import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.correction.enums.CorrectionStatus;
import com.parivahan.backend.correction.enums.CorrectionTargetType;
import com.parivahan.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "correction_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CorrectionRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectionTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 100)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String currentValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestedValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evidenceBase64;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CorrectionStatus status = CorrectionStatus.SUBMITTED;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
