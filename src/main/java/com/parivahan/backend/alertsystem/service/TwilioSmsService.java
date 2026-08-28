package com.parivahan.backend.alertsystem.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS delivery service backed by Twilio.
 *
 * To activate real SMS:
 *   1. Set env vars: TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER
 *   2. Ensure the destination number is verified in your Twilio trial account.
 *
 * If credentials are missing, the service logs a warning and skips delivery gracefully.
 *
 * Future Integration Point: swap Twilio for AWS SNS by replacing the body of sendSms().
 */
@Service
@Slf4j
public class TwilioSmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    private boolean twilioConfigured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            twilioConfigured = true;
            log.info("Twilio SMS service initialized.");
        } else {
            log.warn("Twilio credentials not configured — SMS delivery is disabled. " +
                     "Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER env vars to enable.");
        }
    }

    /**
     * Sends an SMS to the user's mobile number.
     *
     * @param toMobileNumber recipient number in E.164 format (e.g. +919876543210)
     * @param message        the alert message body
     */
    public void sendSms(String toMobileNumber, String message) {
        if (!twilioConfigured) {
            log.warn("[SMS-SKIPPED] To: {} | Message: {}", toMobileNumber, message);
            return;
        }
        try {
            // Normalize to E.164 if user stored without country code
            String to = toMobileNumber.startsWith("+") ? toMobileNumber : "+91" + toMobileNumber;

            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("SMS sent. SID={} To={}", twilioMessage.getSid(), to);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toMobileNumber, e.getMessage());
        }
    }
}
