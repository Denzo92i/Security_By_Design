package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.CustomerRepository;
import com.example.foodndeliv.dto.CustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerResponseDTO;
import com.example.foodndeliv.dto.CustomerUpdateDTO;
import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.types.CustomerState;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO) {
        Customer newCustomer = modelMapper.map(customerRequestDTO, Customer.class);
        
        // FIX : ModelMapper écrase les valeurs par défaut de l'entité
        // On force l'état ACTIVE explicitement ici
        if (newCustomer.getState() == null) {
            newCustomer.setState(CustomerState.ACTIVE);
        }
        
        Customer savedCustomer = customerRepository.save(newCustomer);
        return modelMapper.map(savedCustomer, CustomerResponseDTO.class);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(c -> modelMapper.map(c, CustomerResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public CustomerResponseDTO updateCustomer(Long id, CustomerUpdateDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        modelMapper.map(dto, customer);
        Customer saved = customerRepository.save(customer);
        return modelMapper.map(saved, CustomerResponseDTO.class);
    }
}