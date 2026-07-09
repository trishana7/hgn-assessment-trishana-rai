package com.hgn.sosalertservice.controller;

import com.hgn.sosalertservice.dto.DeviceDto;
import com.hgn.sosalertservice.entity.Device;
import com.hgn.sosalertservice.service.SosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices Controller", description = "Endpoints for managing tracking devices")
public class DeviceController {

    private final SosService sosService;

    public DeviceController(SosService sosService) {
        this.sosService = sosService;
    }

    @PostMapping
    @Operation(summary = "Create a new device", description = "Saves a tracking device to the database")
    public ResponseEntity<Device> createDevice(@RequestBody DeviceDto request) {
        Device device = sosService.createDevice(request);
        return ResponseEntity.ok(device);
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieves all tracking devices in the database")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(sosService.getAllDevices());
    }
}
