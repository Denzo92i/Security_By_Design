package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.OrderRequestDTO;
import com.example.foodndeliv.dto.OrderResponseDTO;
import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.entity.Order;
import com.example.foodndeliv.entity.OrderLine;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.CustomerRepository;
import com.example.foodndeliv.repository.OrderRepository;
import com.example.foodndeliv.repository.RestaurantRepository;
import com.example.foodndeliv.types.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        long pendingOrders = orderRepository.countByCustomer_IdAndState(request.getCustomerId(), OrderState.PENDING);
        if (pendingOrders >= 5) {
            throw new RuntimeException("Customer already has 5 pending orders");
        }

        BigDecimal total = request.getOrderLines().stream()
                .map(line -> line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + request.getRestaurantId()));

        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setRestaurantId(restaurant.getId());
        order.setState(OrderState.PENDING);
        order.setTotal(total);
        order.setOrderLines(request.getOrderLines().stream()
                .map(dto -> {
                    OrderLine line = new OrderLine();
                    line.setProductName(dto.getProductName());
                    line.setQuantity(dto.getQuantity());
                    line.setPrice(dto.getPrice());
                    line.setOrder(order);
                    return line;
                })
                .collect(Collectors.toList()));

        Order savedOrder = orderRepository.save(order);

        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomerId());
        response.setRestaurantId(savedOrder.getRestaurantId());
        response.setTotal(savedOrder.getTotal());
        response.setState(savedOrder.getState().name());
        response.setCreatedAt(savedOrder.getCreatedAt());

        return response;
    }
}