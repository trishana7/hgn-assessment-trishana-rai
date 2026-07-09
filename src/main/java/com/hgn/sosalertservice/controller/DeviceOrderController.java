package com.hgn.sosalertservice.controller;

import com.hgn.sosalertservice.dto.DeviceOrderDto;
import com.hgn.sosalertservice.entity.DeviceOrder;
import com.hgn.sosalertservice.service.SosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-orders")
@Tag(name = "Device Assignments Controller", description = "Endpoints for linking devices to active bookings")
public class DeviceOrderController {

    private final SosService sosService;

    public DeviceOrderController(SosService sosService) {
        this.sosService = sosService;
    }

    @PostMapping
    @Operation(summary = "Map a device to an order", description = "Assigns a tracking device to a specific booking for a date range")
    public ResponseEntity<DeviceOrder> createDeviceOrder(@RequestBody DeviceOrderDto request) {
        DeviceOrder deviceOrder = sosService.createDeviceOrder(request);
        return ResponseEntity.ok(deviceOrder);
    }

    @GetMapping
    @Operation(summary = "Get all device mappings", description = "Retrieves all device-to-order mapping assignments in the database")
    public ResponseEntity<List<DeviceOrder>> getAllDeviceOrders() {
        return ResponseEntity.ok(sosService.getAllDeviceOrders());
    }
}
