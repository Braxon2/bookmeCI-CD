package com.dusanbranovic.bookme.dto.responses;

import java.util.List;
import java.util.UUID;

public record BookableUnitsResponseDTO(
        UUID id,
        int maxCapacity,
        double squareMeters,
        int singleBeds,
        int doubleBeds,
        int maxAdultCapacity,
        int maxKidsCapacity,
        String name,
        List<UnitFascilityResponseDTO> unitFascilityResponseDTOS
) {
}
