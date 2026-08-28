package com.parivahan.backend.alertsystem.controller;

import com.parivahan.backend.alertsystem.dto.AlertSummaryDto;
import com.parivahan.backend.alertsystem.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /** GET /api/v1/alerts — All alerts for the authenticated user, newest first. */
    @GetMapping
    public ResponseEntity<List<AlertSummaryDto>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    /** GET /api/v1/alerts/unread-count — Unread badge count for the notification bell. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(alertService.getUnreadCount());
    }

    /** PATCH /api/v1/alerts/{alertId}/read — Mark a single alert as read. */
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<AlertSummaryDto> markRead(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertService.markRead(alertId));
    }

    /** PATCH /api/v1/alerts/read-all — Mark all alerts as read. */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        alertService.markAllRead();
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/v1/alerts/{alertId} — Dismiss/delete an alert. */
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> dismiss(@PathVariable Long alertId) {
        alertService.dismiss(alertId);
        return ResponseEntity.noContent().build();
    }
}
