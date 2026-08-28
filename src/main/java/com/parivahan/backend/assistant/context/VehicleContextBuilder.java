package com.parivahan.backend.assistant.context;

import com.parivahan.backend.assistant.model.VehicleIntent;
import com.parivahan.backend.dashboard.dto.*;
import com.parivahan.backend.dashboard.dto.ComplianceItemDto.ComplianceStatus;
import com.parivahan.backend.livelocation.dto.VehicleTwinDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Builds a controlled, plain-text vehicle context to be passed to Gemini.
 *
 * SECURITY: This class only receives DTO objects — never raw JPA entities,
 * database credentials, JWT tokens, or internal IDs.
 *
 * The context is filtered by intent so only relevant data is sent to the AI,
 * keeping prompts smaller, safer, and more accurate.
 */
@Component
public class VehicleContextBuilder {

    /**
     * Build a filtered context string based on the detected intent.
     */
    public String build(DashboardResponseDto dashboard, VehicleIntent intent) {
        StringBuilder sb = new StringBuilder();

        switch (intent) {
            case PUC_STATUS -> appendPucContext(sb, dashboard);
            case INSURANCE_STATUS -> appendInsuranceContext(sb, dashboard);
            case TAX_STATUS -> appendTaxContext(sb, dashboard);
            case RC_STATUS -> appendRcContext(sb, dashboard);
            case PERMIT_STATUS -> appendPermitContext(sb, dashboard);
            case FITNESS_STATUS -> appendFitnessContext(sb, dashboard);
            case DOCUMENT_STATUS, EXPIRING_DOCUMENTS -> appendFullComplianceContext(sb, dashboard);
            case HEALTH_SCORE -> appendHealthContext(sb, dashboard);
            case CHALLAN_STATUS, CHALLAN_DETAILS -> appendChallanContext(sb, dashboard);
            case LIVE_LOCATION -> appendLocationContext(sb, dashboard);
            case ACTIVE_ALERTS -> appendAlertsContext(sb, dashboard);
            case WHAT_TO_DO_TODAY, VEHICLE_OVERVIEW -> appendFullContext(sb, dashboard);
            case GENERAL_VEHICLE_QUESTION -> appendVehicleBasicInfo(sb, dashboard);
        }

        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    // Context sections
    // -----------------------------------------------------------------------

    private void appendVehicleBasicInfo(StringBuilder sb, DashboardResponseDto d) {
        VehicleCardDto v = d.getVehicleCard();
        if (v == null) return;
        sb.append("VEHICLE:\n");
        sb.append("  Registration Number: ").append(v.getRegistrationNumber()).append("\n");
        sb.append("  Manufacturer: ").append(v.getManufacturer()).append("\n");
        sb.append("  Model: ").append(v.getModel()).append("\n");
        sb.append("  Fuel Type: ").append(v.getFuelType()).append("\n");
        sb.append("  Vehicle Class: ").append(v.getVehicleClass()).append("\n");
        sb.append("  RTO: ").append(v.getRto()).append("\n");
        sb.append("  Status: ").append(v.getVehicleStatus()).append("\n");
    }

    private void appendPucContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nPUC STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getPuc() : null);
    }

    private void appendInsuranceContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nINSURANCE STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getInsurance() : null);
        if (d.getVehicleCard() != null && d.getVehicleCard().getInsuranceProvider() != null) {
            sb.append("  Insurance Provider: ").append(d.getVehicleCard().getInsuranceProvider()).append("\n");
        }
    }

    private void appendTaxContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nROAD TAX STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getTax() : null);
    }

    private void appendRcContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nRC (REGISTRATION CERTIFICATE) STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getRc() : null);
    }

    private void appendPermitContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nPERMIT STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getPermit() : null);
    }

    private void appendFitnessContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nFITNESS CERTIFICATE STATUS:\n");
        appendComplianceItem(sb, d.getCompliance() != null ? d.getCompliance().getFitness() : null);
    }

    private void appendFullComplianceContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        ComplianceStatusDto c = d.getCompliance();
        if (c == null) {
            sb.append("\nCOMPLIANCE: No compliance data available.\n");
            return;
        }
        sb.append("\nCOMPLIANCE DOCUMENTS:\n");
        sb.append("  RC: "); appendComplianceItem(sb, c.getRc());
        sb.append("  PUC: "); appendComplianceItem(sb, c.getPuc());
        sb.append("  Insurance: "); appendComplianceItem(sb, c.getInsurance());
        sb.append("  Road Tax: "); appendComplianceItem(sb, c.getTax());
        sb.append("  Permit: "); appendComplianceItem(sb, c.getPermit());
        sb.append("  Fitness: "); appendComplianceItem(sb, c.getFitness());
    }

    private void appendHealthContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nHEALTH SCORE:\n");
        sb.append("  Score: ").append(d.getHealthScore()).append("/100\n");
        sb.append("  Label: ").append(d.getHealthLabel()).append("\n");
        sb.append("\nHEALTH FACTORS (compliance documents):\n");
        ComplianceStatusDto c = d.getCompliance();
        if (c != null) {
            sb.append("  Insurance (25 pts weight): "); appendComplianceItem(sb, c.getInsurance());
            sb.append("  PUC (25 pts weight): "); appendComplianceItem(sb, c.getPuc());
            sb.append("  Road Tax (20 pts weight): "); appendComplianceItem(sb, c.getTax());
            sb.append("  RC (15 pts weight): "); appendComplianceItem(sb, c.getRc());
            sb.append("  Permit (8 pts weight): "); appendComplianceItem(sb, c.getPermit());
            sb.append("  Fitness (7 pts weight): "); appendComplianceItem(sb, c.getFitness());
        }
        appendChallanSummaryLine(sb, d.getPendingChallans());
    }

    private void appendChallanContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nCHALLAN INFORMATION:\n");
        List<ChallanDto> challans = d.getPendingChallans();
        if (challans == null || challans.isEmpty()) {
            sb.append("  No pending challans.\n");
        } else {
            sb.append("  Total Pending Challans: ").append(challans.size()).append("\n");
            double total = challans.stream().mapToDouble(c -> c.getAmount().doubleValue()).sum();
            sb.append("  Total Pending Amount: ₹").append(String.format("%.0f", total)).append("\n");
            sb.append("  Challan Details:\n");
            for (ChallanDto c : challans) {
                sb.append("    - Offence: ").append(c.getOffence())
                        .append(", Amount: ₹").append(String.format("%.0f", c.getAmount().doubleValue()))
                        .append(", Date: ").append(c.getChallanDate()).append("\n");
            }
        }
    }

    private void appendLocationContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nGPS / LIVE LOCATION:\n");
        VehicleTwinDto gps = d.getVehicleTwin();
        if (gps == null) {
            sb.append("  GPS Status: OFFLINE (no location data available)\n");
        } else {
            sb.append("  GPS Status: ONLINE\n");
            if (gps.getAddress() != null) sb.append("  Current Address: ").append(gps.getAddress()).append("\n");
            if (gps.getLatitude() != null) sb.append("  Latitude: ").append(gps.getLatitude()).append("\n");
            if (gps.getLongitude() != null) sb.append("  Longitude: ").append(gps.getLongitude()).append("\n");
            if (gps.getSpeed() != null) sb.append("  Speed: ").append(gps.getSpeed()).append(" km/h\n");
            if (gps.getLastUpdated() != null) sb.append("  Last Updated: ").append(gps.getLastUpdated()).append("\n");
        }
    }

    private void appendAlertsContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nACTIVE ALERTS:\n");
        List<AlertDto> alerts = d.getAlerts();
        if (alerts == null || alerts.isEmpty()) {
            sb.append("  No active alerts.\n");
        } else {
            for (AlertDto a : alerts) {
                sb.append("  [").append(a.getType()).append("] ")
                        .append(a.getTitle()).append(": ").append(a.getMessage()).append("\n");
            }
        }
    }

    private void appendFullContext(StringBuilder sb, DashboardResponseDto d) {
        appendVehicleBasicInfo(sb, d);
        sb.append("\nHEALTH SCORE:\n");
        sb.append("  Score: ").append(d.getHealthScore()).append("/100\n");
        sb.append("  Label: ").append(d.getHealthLabel()).append("\n");
        appendFullComplianceContext(sb, d);
        appendChallanContext(sb, d);
        appendLocationContext(sb, d);
        appendAlertsContext(sb, d);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void appendComplianceItem(StringBuilder sb, ComplianceItemDto item) {
        if (item == null || item.getStatus() == ComplianceStatus.NOT_APPLICABLE) {
            sb.append("Not Applicable\n");
            return;
        }
        sb.append(item.getStatus().name());
        if (item.getValidTill() != null) {
            sb.append(" (Valid till: ").append(item.getValidTill());
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), item.getValidTill());
            if (daysLeft >= 0) {
                sb.append(", ").append(daysLeft).append(" days remaining");
            } else {
                sb.append(", expired ").append(Math.abs(daysLeft)).append(" days ago");
            }
            sb.append(")");
        }
        sb.append("\n");
    }

    private void appendChallanSummaryLine(StringBuilder sb, List<ChallanDto> challans) {
        if (challans == null || challans.isEmpty()) {
            sb.append("\nCHALLANS: No pending challans.\n");
        } else {
            double total = challans.stream().mapToDouble(c -> c.getAmount().doubleValue()).sum();
            sb.append("\nCHALLANS: ").append(challans.size())
                    .append(" pending, total amount ₹").append(String.format("%.0f", total)).append("\n");
        }
    }
}
