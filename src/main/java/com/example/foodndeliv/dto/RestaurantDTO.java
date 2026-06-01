package com.example.foodndeliv.dto;

import lombok.Data;
import com.example.foodndeliv.types.*;

@Data
public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private RestaurantState state;
}
