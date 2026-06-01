package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "orders")
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderDetailsContaining(String orderDetails);

    @Query("SELECT o FROM Order o WHERE o.restaurant.name = :restaurantName")
    List<Order> findOrdersByRestaurantName(@Param("restaurantName") String restaurantName);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :custID")
    List<Order> findOrdersByCustID(@Param("custID") Long custID);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :custID and o.restaurant.id = :restID")
    List<Order> findOrdersByCustRestID(@Param("custID") Long custID, @Param("restID") Long restID);


}




