package com.parivahan.backend.challan.controller;

import com.parivahan.backend.challan.dto.ChallanDetailDto;
import com.parivahan.backend.challan.dto.ChallanSummaryDto;
import com.parivahan.backend.challan.dto.PaymentReceiptDto;
import com.parivahan.backend.challan.enums.ChallanStatus;
import com.parivahan.backend.challan.service.ChallanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/challans")
@RequiredArgsConstructor
public class ChallanController {

    private final ChallanService challanService;

    /** GET /api/v1/challans — All challans for the current user across all vehicles. */
    @GetMapping
    public ResponseEntity<List<ChallanSummaryDto>> getAllChallans() {
        return ResponseEntity.ok(challanService.getAllChallansForCurrentUser());
    }

    /** GET /api/v1/challans/{challanId} — Full detail of a single challan. */
    @GetMapping("/{challanId}")
    public ResponseEntity<ChallanDetailDto> getChallanDetail(@PathVariable Long challanId) {
        return ResponseEntity.ok(challanService.getChallanDetail(challanId));
    }

    /** GET /api/v1/challans/{challanId}/status — Quick status check. */
    @GetMapping("/{challanId}/status")
    public ResponseEntity<Map<String, ChallanStatus>> getChallanStatus(@PathVariable Long challanId) {
        return ResponseEntity.ok(Map.of("status", challanService.getChallanStatus(challanId)));
    }

    /** POST /api/v1/challans/{challanId}/pay — Pay a challan online. */
    @PostMapping("/{challanId}/pay")
    public ResponseEntity<PaymentReceiptDto> payChallan(@PathVariable Long challanId) {
        return ResponseEntity.ok(challanService.payChallan(challanId));
    }

    /**
     * GET /api/v1/challans/{challanId}/download
     * Returns a mock "challan document" as a downloadable JSON response.
     * Future: generate a real PDF here.
     */
    @GetMapping("/{challanId}/download")
    public ResponseEntity<ChallanDetailDto> downloadChallan(@PathVariable Long challanId) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"challan-" + challanId + ".json\"")
                .body(challanService.getChallanDetail(challanId));
    }

    /**
     * GET /api/v1/challans/{challanId}/receipt
     * Returns payment receipt — only available for PAID challans.
     */
    @GetMapping("/{challanId}/receipt")
    public ResponseEntity<PaymentReceiptDto> downloadReceipt(@PathVariable Long challanId) {
        ChallanDetailDto detail = challanService.getChallanDetail(challanId);
        if (detail.getStatus() != ChallanStatus.PAID) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"receipt-" + challanId + ".json\"")
                .body(challanService.payChallan(challanId)); // re-uses receipt DTO builder
    }
}
