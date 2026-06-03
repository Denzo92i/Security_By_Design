package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.Order;
import com.example.foodndeliv.types.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByCustomer_IdAndState(Long customerId, OrderState state);
}