package com.parivahan.backend.alertsystem.entity;

import com.parivahan.backend.alertsystem.enums.AlertSeverity;
import com.parivahan.backend.alertsystem.enums.AlertType;
import com.parivahan.backend.common.entity.BaseEntity;
import com.parivahan.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Alert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Reference to the related entity (e.g. vehicleId, challanId)
    private Long referenceId;

    // Comma-separated delivery channels: "IN_APP", "SMS"
    private String channels;

    @Builder.Default
    private boolean read = false;
}
