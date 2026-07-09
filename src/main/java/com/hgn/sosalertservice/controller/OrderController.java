package com.hgn.sosalertservice.controller;

import com.hgn.sosalertservice.dto.OrderDto;
import com.hgn.sosalertservice.entity.Order;
import com.hgn.sosalertservice.service.SosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders Controller", description = "Endpoints for managing trek orders/bookings")
public class OrderController {

    private final SosService sosService;

    public OrderController(SosService sosService) {
        this.sosService = sosService;
    }

    @PostMapping
    @Operation(summary = "Create a new trek order", description = "Saves a new trek order with its associated group members")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto request) {
        Order order = sosService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all trek orders/bookings in the database")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(sosService.getAllOrders());
    }
}
