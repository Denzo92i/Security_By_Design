package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.Order;
import com.example.foodndeliv.types.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByCustomerIdAndState(Long customerId, OrderState state);
    long countByRestaurantIdAndState(Long restaurantId, OrderState state);
}