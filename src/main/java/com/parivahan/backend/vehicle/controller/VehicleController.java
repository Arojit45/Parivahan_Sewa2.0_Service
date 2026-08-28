package com.parivahan.backend.vehicle.controller;

import com.parivahan.backend.vehicle.dto.VehicleOwnerResponse;
import com.parivahan.backend.vehicle.dto.VehiclePublicResponse;
import com.parivahan.backend.vehicle.dto.InitRegisterRequest;
import com.parivahan.backend.vehicle.dto.VerifyRegisterRequest;
import java.util.Map;

import com.parivahan.backend.vehicle.service.CaptchaService;
import com.parivahan.backend.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Validated
public class VehicleController {

    private final VehicleService vehicleService;
    private final CaptchaService captchaService;

    @PostMapping("/init-register")
    public ResponseEntity<Map<String, String>> initRegister(
            @RequestHeader("X-Captcha-Id") String captchaId,
            @RequestHeader("X-Captcha-Answer") String captchaAnswer,
            @Valid @RequestBody InitRegisterRequest request) {
        
        if (!captchaService.validateCaptcha(captchaId, captchaAnswer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String message = vehicleService.initRegistration(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/verify-register")
    public ResponseEntity<VehicleOwnerResponse> verifyRegister(
            @RequestHeader("X-Captcha-Id") String captchaId,
            @RequestHeader("X-Captcha-Answer") String captchaAnswer,
            @Valid @RequestBody VerifyRegisterRequest request) {
        
        if (!captchaService.validateCaptcha(captchaId, captchaAnswer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        VehicleOwnerResponse response = vehicleService.verifyRegistration(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<VehiclePublicResponse> getVehicleInfo(
            @RequestHeader("X-Captcha-Id") String captchaId,
            @RequestHeader("X-Captcha-Answer") String captchaAnswer,
            @PathVariable @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$", message = "Invalid Indian registration number format (e.g., MH12AB1234)") String registrationNumber) {
        
        if (!captchaService.validateCaptcha(captchaId, captchaAnswer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        VehiclePublicResponse response = vehicleService.getVehicleInfo(registrationNumber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{registrationNumber}")
    public ResponseEntity<Void> removeVehicle(@PathVariable String registrationNumber) {
        vehicleService.removeVehicle(registrationNumber);
        return ResponseEntity.noContent().build();
    }
}
