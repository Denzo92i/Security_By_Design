package com.example.foodndeliv.controller;

import com.example.foodndeliv.service.*;
import com.example.foodndeliv.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ctrl/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // --- AJOUT : Point d'entrée pour récupérer les clients ---
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDTO createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        return customerService.createCustomer(customerRequestDTO);
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerResponseDTO updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO) {
        return customerService.updateCustomer(id, customerUpdateDTO);
    }
}