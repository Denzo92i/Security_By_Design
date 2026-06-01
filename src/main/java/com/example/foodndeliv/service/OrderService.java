package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.*;
import com.example.foodndeliv.dto.*;
import com.example.foodndeliv.entity.*;
import com.example.foodndeliv.types.OrderState;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;
import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Invariant: Maximum 5 pending orders per customer
    private static final int MAX_PENDING_ORDERS_PER_CUSTOMER = 5;

    @Transactional
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        // Invariant validation: Check pending orders count
        long pendingOrdersCount = orderRepository.findAll().stream()
                .filter(o -> o.getCustomer().getId().equals(orderRequestDTO.getCustomerId()))
                .filter(o -> o.getState() == OrderState.PENDING)
                .count();
        
        if (pendingOrdersCount >= MAX_PENDING_ORDERS_PER_CUSTOMER) {
            throw new RuntimeException("Customer has too many pending orders (max: " + 
                    MAX_PENDING_ORDERS_PER_CUSTOMER + ")");
        }

        Order order = new Order();

        Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Restaurant restaurant = restaurantRepository.findById(orderRequestDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        
        // CORRECTION : Initialisation forcée à PENDING pour éviter l'erreur PropertyValueException
        order.setState(OrderState.PENDING);

        List<OrderLine> orderLines = new ArrayList<>();
        double totalPrice = 0;

        for(OrderLineDTO orderlinedto : orderRequestDTO.getOrderLines())
        {
                OrderLine orderLine = modelMapper.map(orderlinedto, OrderLine.class);
                orderLine.setOrder(order);
                orderLines.add(orderLine);
                
                // Server-side total calculation
                totalPrice += orderLine.getPrice() * orderLine.getQuantity();
        }

        order.setOrderLines(orderLines);
        
        // Set server-calculated timestamp
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order newOrder = orderRepository.save(order);

        // Map to response DTO with calculated total
        OrderResponseDTO responseDTO = modelMapper.map(newOrder, OrderResponseDTO.class);
        responseDTO.setTotalPrice(totalPrice);
        
        return responseDTO;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public List<OrderResponseDTO> getAllOrders() {

        List<OrderResponseDTO> retOrders = new ArrayList<>();

        for(Order order : orderRepository.findAll())
        {
                OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
                
                // Calculate total price server-side
                double totalPrice = order.getOrderLines().stream()
                    .mapToDouble(ol -> ol.getPrice() * ol.getQuantity())
                    .sum();
                dto.setTotalPrice(totalPrice);
                
                retOrders.add(dto);
        }
        return retOrders;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
        
        // Calculate total price server-side
        double totalPrice = order.getOrderLines().stream()
            .mapToDouble(ol -> ol.getPrice() * ol.getQuantity())
            .sum();
        dto.setTotalPrice(totalPrice);
        
        return dto;
    }
}