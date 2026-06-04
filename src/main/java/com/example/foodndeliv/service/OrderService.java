package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.OrderRequestDTO;
import com.example.foodndeliv.dto.OrderResponseDTO;
import com.example.foodndeliv.entity.Order;
import com.example.foodndeliv.entity.OrderLine;
import com.example.foodndeliv.repository.OrderRepository;
import com.example.foodndeliv.types.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or " +
        "(hasRole('ROLE_CUSTOMER') and @securityService.isOwner(#request.customerId, authentication))"
    )
    public OrderResponseDTO createOrder(OrderRequestDTO request, Authentication authentication) {

        // ── Validation manuelle des orderLines (Bean Validation fallback) ──
        Map<String, String> validationErrors = new HashMap<>();
        if (request.getOrderLines() != null) {
            for (int i = 0; i < request.getOrderLines().size(); i++) {
                var line = request.getOrderLines().get(i);
                if (line.getProductName() == null || line.getProductName().trim().isEmpty()) {
                    validationErrors.put("orderLines[" + i + "].productName", "Product name must not be blank");
                }
                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    validationErrors.put("orderLines[" + i + "].quantity", "Quantity must be positive");
                }
                if (line.getPrice() == null || line.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                    validationErrors.put("orderLines[" + i + "].price", "Price must be at least 0.01");
                }
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationErrors.toString());
        }

        // ── Invariant métier : max 5 commandes PENDING par client ──
        long pendingCount = orderRepository.countByCustomerIdAndState(
                request.getCustomerId(), OrderState.PENDING);
        if (pendingCount >= 5) {
            throw new RuntimeException("Customer already has 5 pending orders (max: 5)");
        }

        // ── Calcul du total côté serveur (Task 3b-ii) ──
        BigDecimal total = request.getOrderLines().stream()
                .map(line -> line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setRestaurantId(request.getRestaurantId());
        order.setState(OrderState.PENDING);
        order.setTotal(total);

        List<OrderLine> orderLines = request.getOrderLines().stream()
                .map(dto -> {
                    OrderLine line = new OrderLine();
                    line.setProductName(dto.getProductName());
                    line.setQuantity(dto.getQuantity());
                    line.setPrice(dto.getPrice());
                    line.setOrder(order);
                    return line;
                })
                .collect(Collectors.toList());

        order.setOrderLines(orderLines);
        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return toResponse(order);
    }

    public long countPendingOrdersForCustomer(Long customerId) {
        return orderRepository.countByCustomerIdAndState(customerId, OrderState.PENDING);
    }

    public long countPendingOrdersForRestaurant(Long restaurantId) {
        return orderRepository.countByRestaurantIdAndState(restaurantId, OrderState.PENDING);
    }

    private OrderResponseDTO toResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setRestaurantId(order.getRestaurantId());
        response.setTotal(order.getTotal());
        response.setState(order.getState().name());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}