package com.example.foodndeliv.controller;

import com.example.foodndeliv.service.CustomerService;
import com.example.foodndeliv.dto.CustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerResponseDTO;
import com.example.foodndeliv.dto.CustomerUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ctrl/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // Authentication injecté pour stocker le keycloakId à la création
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDTO createCustomer(
            @Valid @RequestBody CustomerRequestDTO dto,
            Authentication authentication) {
        return customerService.createCustomer(dto, authentication);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerResponseDTO updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDTO dto) {
        return customerService.updateCustomer(id, dto);
    }

    // ── Task 3b-iii + v : Désactivation avec invariant + ABAC owner ──
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCustomer(
            @PathVariable Long id,
            Authentication authentication) {
        customerService.deactivateCustomer(id, authentication);
        return ResponseEntity.noContent().build();
    }
}