package com.example.foodndeliv.dto;

import lombok.Data;
import com.example.foodndeliv.types.*;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private CustomerState state;
}

