package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.CustomerRepository;
import com.example.foodndeliv.repository.OrderRepository;
import com.example.foodndeliv.dto.CustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerResponseDTO;
import com.example.foodndeliv.dto.CustomerUpdateDTO;
import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.types.CustomerState;
import com.example.foodndeliv.types.OrderState;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ── Création : on stocke le "sub" du JWT comme keycloakId ──
    // C'est ce qui permet ensuite à SecurityService.isOwner() de fonctionner.
    @Transactional
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto, Authentication authentication) {
        Customer newCustomer = modelMapper.map(dto, Customer.class);

        if (newCustomer.getState() == null) {
            newCustomer.setState(CustomerState.ACTIVE);
        }

        // Stocker le sub Keycloak pour l'ABAC
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            newCustomer.setKeycloakId(jwt.getSubject());
        }

        Customer saved = customerRepository.save(newCustomer);
        return modelMapper.map(saved, CustomerResponseDTO.class);
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

    // ── Task 3b-iii + v : Invariant désactivation + ABAC owner ──
    // ADMIN : peut désactiver n'importe quel compte.
    // CUSTOMER : peut désactiver uniquement son propre compte (vérifié via keycloakId).
    // Bloqué si des commandes PENDING existent pour ce customer.
    @Transactional
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or " +
        "(hasRole('ROLE_CUSTOMER') and @securityService.isOwner(#customerId, authentication))"
    )
    public void deactivateCustomer(Long customerId, Authentication authentication) {

        // ── Invariant métier ──
        long pendingOrders = orderRepository.countByCustomerIdAndState(
                customerId, OrderState.PENDING);
        if (pendingOrders > 0) {
            throw new IllegalStateException(
                "Cannot deactivate customer with " + pendingOrders + " pending order(s). " +
                "Please cancel or complete all pending orders first.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        customer.setState(CustomerState.INACTIVE);
        customerRepository.save(customer);
    }
}