package com.dusanbranovic.bookme.dto.responses;


import java.util.List;
import java.util.UUID;

public record PropertyDTO(
        UUID publicId,
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
