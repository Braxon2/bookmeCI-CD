package com.dusanbranovic.bookme.dto.requests;

import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PropertyRequestDTO(

        @NotNull(message = "Property type is required")
        @Valid
        PropertyTypeDTO propertyTypeDTO,

        @NotBlank(message = "Property name is required")
        @Size(min = 2, max = 100, message = "Property name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Property description is required")
        @Size(min = 10, max = 3000, message = "Property description must be between 10 and 3000 characters")
        String description,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Size(max = 2000, message = "House rules must not exceed 2000 characters")
        String houseRules,

        @Size(max = 2000, message = "Important information must not exceed 2000 characters")
        String importantInfo,

        @NotNull(message = "Facilities list must not be null")
        List<@Valid FascilityResponseDTO> fascilitiesDTO
) {
}
