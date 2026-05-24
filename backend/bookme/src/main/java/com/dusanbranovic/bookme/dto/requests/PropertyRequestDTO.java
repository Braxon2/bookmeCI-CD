package com.dusanbranovic.bookme.dto.requests;

import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;

import java.util.List;

public record PropertyRequestDTO(
        PropertyTypeDTO propertyTypeDTO,
        String name,
        String description,
        String country,
        String city,
        String address,
        String houseRules,
        String importantInfo,
        List<FascilityResponseDTO> fascilitiesDTO
) {
}
