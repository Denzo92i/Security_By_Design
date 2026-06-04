package com.example.foodndeliv.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RestaurantResponseDTO {

    private Long id;
    private String name;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}