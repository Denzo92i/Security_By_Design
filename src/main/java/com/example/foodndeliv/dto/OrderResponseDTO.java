package com.example.foodndeliv.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal total;
    private String state;
    private LocalDateTime createdAt;
}