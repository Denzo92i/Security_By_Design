package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.RestaurantRequestDTO;
import com.example.foodndeliv.dto.RestaurantResponseDTO;
import com.example.foodndeliv.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ctrl/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    // ── ADMIN only (Task 3b-iv) ──
    @PostMapping
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(
            @Valid @RequestBody RestaurantRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(dto));
    }

    // ── CUSTOMER or ADMIN ──
    @GetMapping
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // ── ADMIN only + invariant pending orders (Task 3b-iii + iv) ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeRestaurant(@PathVariable Long id) {
        restaurantService.closeRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    // ── ADMIN only ──
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDTO dto) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, dto));
    }
}