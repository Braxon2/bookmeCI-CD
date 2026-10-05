package com.dusanbranovic.bookme.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PropertyTypeRequestDTO(

        @NotBlank(message = "Property type name cannot be empty")
        @Size(
                min = 2,
                max = 100,
                message = "Property type name must be between 2 and 100 characters"
        )
        String name
        
) {
}
