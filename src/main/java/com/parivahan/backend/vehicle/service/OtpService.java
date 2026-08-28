package com.parivahan.backend.vehicle.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String identifier) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));
        otpCache.put(identifier, otp);
        
        // Simulate sending OTP
        log.info("======================================");
        log.info("MOCK OTP SENT for {}: {}", identifier, otp);
        log.info("======================================");
        
        return otp;
    }

    public boolean verifyOtp(String identifier, String providedOtp) {
        if (identifier == null || providedOtp == null) {
            return false;
        }
        
        String storedOtp = otpCache.get(identifier);
        if (storedOtp != null && storedOtp.equals(providedOtp.trim())) {
            otpCache.remove(identifier);
            return true;
        }
        
        return false;
    }
}
