package com.parivahan.backend.challan.service;

import com.parivahan.backend.challan.enums.DisputeStatus;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates an authority decision on a challan dispute.
 * This class is designed to be a clean swap point for a real
 * government grievance/authority API integration in the future.
 *
 * Future integration: Replace the body of {@code decide()} with an
 * HTTP call to the government grievance portal API.
 */
@Component
public class MockAuthorityDecisionEngine {

    private final Random random = new Random();

    /**
     * Simulates a decision — randomly returns UPHELD or CANCELLED.
     *
     * @return the authority's decision status
     */
    public DisputeStatus decide() {
        return random.nextBoolean() ? DisputeStatus.UPHELD : DisputeStatus.CANCELLED;
    }

    public String getDecisionMessage(DisputeStatus decision) {
        return switch (decision) {
            case UPHELD -> "After reviewing the evidence and CCTV footage, the authority has upheld the challan. The violation was confirmed. Please proceed with payment.";
            case CANCELLED -> "After reviewing the submitted evidence, the authority has cancelled the challan. No action is required from you.";
            default -> "Decision recorded.";
        };
    }
}
