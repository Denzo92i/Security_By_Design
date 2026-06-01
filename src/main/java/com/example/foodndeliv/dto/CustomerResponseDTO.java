package com.example.foodndeliv.dto;

import lombok.Data;
import com.example.foodndeliv.types.*;

@Data
public class CustomerResponseDTO {
    private Long id;
    private String name;
    private String email;
    private CustomerState state;
}

