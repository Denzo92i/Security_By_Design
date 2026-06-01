package com.example.foodndeliv.dto;

import lombok.Data;
import com.example.foodndeliv.types.*;

@Data
public class CustomerUpdateDTO {
    private String email;
    private CustomerState state;
}

