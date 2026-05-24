package com.dusanbranovic.bookme.dto.responses;

import java.util.List;

public record BookableUnitsResponseDTO(
        Long id,
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
