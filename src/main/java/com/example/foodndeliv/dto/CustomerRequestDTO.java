package com.example.foodndeliv.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CustomerRequestDTO {
    
    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;
}

