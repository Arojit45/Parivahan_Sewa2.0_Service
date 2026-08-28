package com.parivahan.backend.dashboard.service;

import com.parivahan.backend.dashboard.dto.ComplianceItemDto;
import com.parivahan.backend.dashboard.dto.ComplianceItemDto.ComplianceStatus;
import com.parivahan.backend.dashboard.dto.ComplianceStatusDto;
import org.springframework.stereotype.Component;

/**
 * Calculates a vehicle health score out of 100 based on compliance document states.
 *
 * Weights:
 *   Insurance  → 25 pts
 *   PUC        → 25 pts
 *   Tax        → 20 pts
 *   RC         → 15 pts
 *   Permit     → 7.5 pts (N/A counts as full)
 *   Fitness    → 7.5 pts (N/A counts as full)
 */
@Component
public class HealthScoreCalculator {

    public int calculate(ComplianceStatusDto compliance) {
        int score = 0;
        score += score(compliance.getInsurance(), 25, 15);
        score += score(compliance.getPuc(), 25, 15);
        score += score(compliance.getTax(), 20, 10);
        score += score(compliance.getRc(), 15, 10);
        score += scoreNullable(compliance.getPermit(), 8, 4);
        score += scoreNullable(compliance.getFitness(), 7, 3);
        return Math.min(score, 100);
    }

    private int score(ComplianceItemDto item, int full, int partial) {
        if (item == null) return 0;
        return switch (item.getStatus()) {
            case VALID -> full;
            case EXPIRING_SOON -> partial;
            case EXPIRED -> 0;
            default -> 0;
        };
    }

    private int scoreNullable(ComplianceItemDto item, int full, int partial) {
        if (item == null || item.getStatus() == ComplianceStatus.NOT_APPLICABLE) return full;
        return score(item, full, partial);
    }

    public String getHealthLabel(int score) {
        if (score >= 85) return "Vehicle Healthy";
        if (score >= 60) return "Needs Attention";
        return "Critical";
    }
}
