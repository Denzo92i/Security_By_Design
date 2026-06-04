package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.OrderRequestDTO;
import com.example.foodndeliv.dto.OrderResponseDTO;
import com.example.foodndeliv.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ctrl/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ── Task 3b-v : Authentication injecté pour ABAC dans OrderService ──
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            Authentication authentication) {
        OrderResponseDTO response = orderService.createOrder(orderRequest, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}