package com.example.foodndeliv.controller;

import com.example.foodndeliv.service.*;
import com.example.foodndeliv.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ctrl/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.createOrder(orderRequestDTO);
    }
    
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.getAllOrders();
    }
    
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}