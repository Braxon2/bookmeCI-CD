package com.dusanbranovic.bookme.dto.responses;

import java.util.List;

public record BookableUnitFacilitiesResponseDTO(
        Long unitId,
        List<UnitFascilityResponseDTO> facilities
) {
}
