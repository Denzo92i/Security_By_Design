package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "restaurants")
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}

