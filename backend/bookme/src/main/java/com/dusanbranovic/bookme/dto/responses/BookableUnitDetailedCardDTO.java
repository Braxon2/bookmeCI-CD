package com.dusanbranovic.bookme.dto.responses;

import java.util.List;
import java.util.UUID;

public record BookableUnitDetailedCardDTO(
        UUID unitId,
        PropertyDTO propertyDTO,
        List<PeriodPriceDTO> periodPriceList,
        List<AddonResponseDTO> addonList,
        List<UnitFascilityResponseDTO> unitFascilityDTO,
        List<ImageResponseDTO> images,
        int maxCapacity,
        double squareMeters,
        int singleBeds,
        int doubleBeds,
        int maxAdultCapacity,
        int maxKidsCapacity,
        String name
) {
}
