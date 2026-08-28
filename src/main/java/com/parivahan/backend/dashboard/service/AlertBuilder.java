package com.parivahan.backend.dashboard.service;

import com.parivahan.backend.dashboard.dto.AlertDto;
import com.parivahan.backend.dashboard.dto.AlertDto.AlertType;
import com.parivahan.backend.dashboard.dto.ComplianceItemDto;
import com.parivahan.backend.dashboard.dto.ComplianceItemDto.ComplianceStatus;
import com.parivahan.backend.dashboard.dto.ComplianceStatusDto;
import com.parivahan.backend.challan.entity.Challan;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a prioritized list of alerts from compliance status and pending challans.
 */
@Component
public class AlertBuilder {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public List<AlertDto> build(ComplianceStatusDto compliance, List<Challan> pendingChallans) {
        List<AlertDto> alerts = new ArrayList<>();

        checkItem(alerts, compliance.getInsurance(), "Insurance");
        checkItem(alerts, compliance.getPuc(), "PUC Certificate");
        checkItem(alerts, compliance.getTax(), "Road Tax");
        checkItem(alerts, compliance.getRc(), "RC");
        checkItem(alerts, compliance.getPermit(), "Permit");
        checkItem(alerts, compliance.getFitness(), "Fitness Certificate");

        for (Challan c : pendingChallans) {
            alerts.add(AlertDto.builder()
                    .type(AlertType.INFO)
                    .title("Pending Challan")
                    .message(String.format("Offence: %s | Amount: ₹%.0f | Date: %s",
                            c.getOffence(), c.getAmount(), c.getChallanDate().format(DISPLAY_DATE)))
                    .build());
        }

        return alerts;
    }

    private void checkItem(List<AlertDto> alerts, ComplianceItemDto item, String documentName) {
        if (item == null || item.getStatus() == ComplianceStatus.NOT_APPLICABLE) return;

        if (item.getStatus() == ComplianceStatus.EXPIRED) {
            alerts.add(AlertDto.builder()
                    .type(AlertType.CRITICAL)
                    .title(documentName + " Expired")
                    .message(documentName + " expired on " + item.getValidTill().format(DISPLAY_DATE))
                    .build());
        } else if (item.getStatus() == ComplianceStatus.EXPIRING_SOON) {
            alerts.add(AlertDto.builder()
                    .type(AlertType.WARNING)
                    .title(documentName + " Expiring Soon")
                    .message(documentName + " expires on " + item.getValidTill().format(DISPLAY_DATE))
                    .build());
        }
    }
}
