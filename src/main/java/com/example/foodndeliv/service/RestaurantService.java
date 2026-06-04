package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.RestaurantRequestDTO;
import com.example.foodndeliv.dto.RestaurantResponseDTO;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.OrderRepository;
import com.example.foodndeliv.repository.RestaurantRepository;
import com.example.foodndeliv.types.OrderState;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ── Task 3b-iv : RBAC — seuls les admins créent des restaurants ──
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto) {
        Restaurant restaurant = modelMapper.map(dto, Restaurant.class);
        restaurant.setActive(true);
        Restaurant saved = restaurantRepository.save(restaurant);
        return modelMapper.map(saved, RestaurantResponseDTO.class);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public List<RestaurantResponseDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(r -> modelMapper.map(r, RestaurantResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public RestaurantResponseDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        return modelMapper.map(restaurant, RestaurantResponseDTO.class);
    }

    // ── Task 3b-iii + iv : RBAC admin + invariant fermeture ──
    // Impossible de fermer un restaurant avec des commandes PENDING.
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void closeRestaurant(Long id) {
        long pendingOrders = orderRepository.countByRestaurantIdAndState(id, OrderState.PENDING);
        if (pendingOrders > 0) {
            throw new IllegalStateException(
                "Cannot close restaurant with " + pendingOrders + " pending order(s). " +
                "Please wait for all orders to be completed or cancelled.");
        }
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        // Fermeture via active=false (pas de RestaurantState dans l'entité)
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    // ── Task 3b-iv : seuls les admins mettent à jour un restaurant ──
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        modelMapper.map(dto, restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        return modelMapper.map(saved, RestaurantResponseDTO.class);
    }
}