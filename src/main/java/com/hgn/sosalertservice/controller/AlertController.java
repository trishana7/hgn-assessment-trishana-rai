package com.hgn.sosalertservice.controller;

import com.hgn.sosalertservice.dto.AlertResponseDto;
import com.hgn.sosalertservice.dto.ClaimRequestDto;
import com.hgn.sosalertservice.dto.SosRequestDto;
import com.hgn.sosalertservice.service.SosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "SOS Alerts Controller", description = "Endpoints for receiving GPS SOS signals, retrieving and claiming alerts")
public class AlertController {

    private final SosService sosService;

    public AlertController(SosService sosService) {
        this.sosService = sosService;
    }

    @PostMapping
    @Operation(summary = "Receive GPS SOS signal", description = "Finds device and active booking, validates duplicates, and saves/returns the alert")
    public ResponseEntity<AlertResponseDto> receiveSos(@RequestBody SosRequestDto request) {
        AlertResponseDto response = sosService.receiveSos(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all alerts", description = "Retrieves all SOS alerts generated in the system")
    public ResponseEntity<List<AlertResponseDto>> getAllAlerts() {
        return ResponseEntity.ok(sosService.getAllAlerts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID", description = "Retrieves a specific alert by its unique database ID")
    public ResponseEntity<AlertResponseDto> getAlertById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sosService.getAlertById(id));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Claim an alert", description = "Allows a coordinator to claim an open alert, preventing concurrent claim updates via pessimistic locking")
    public ResponseEntity<AlertResponseDto> claimAlert(
            @PathVariable("id") Long id,
            @RequestBody ClaimRequestDto request
    ) {
        AlertResponseDto response = sosService.claimAlert(id, request.getCoordinator());
        return ResponseEntity.ok(response);
    }
}
