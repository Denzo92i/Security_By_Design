package com.example.foodndeliv.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.example.foodndeliv.types.OrderState;

@Data
public class OrderRequestDTO {

    @NotNull(message = "Customer ID must not be null")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    @NotNull(message = "Restaurant ID must not be null")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;
    
    @NotEmpty(message = "Order lines must not be empty")
    @Valid
    private List<OrderLineDTO> orderLines;

    // AJOUTER CE CHAMP
    @NotNull(message = "Order state must not be null")
    private OrderState state;
}