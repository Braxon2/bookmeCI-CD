package com.dusanbranovic.bookme.dto.responses;

import java.util.List;
import java.util.UUID;

public record BookableUnitFacilitiesResponseDTO(
        UUID unitId,
        List<UnitFascilityResponseDTO> facilities
) {
}
