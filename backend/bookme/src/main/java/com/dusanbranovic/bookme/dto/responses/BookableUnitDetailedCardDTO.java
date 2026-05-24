package com.dusanbranovic.bookme.dto.responses;

import java.util.List;

public record BookableUnitDetailedCardDTO(
        Long unitId,
        PropertyDTO propertyDTO,
        List<PeriodPriceDTO> periodPriceList,
        List<AddonResponseDTO> addonList,
        List<UnitFascilityResponseDTO> unitFascilityDTO,
        List<UnitImageDTO> images,
        int maxCapacity,
        double squareMeters,
        int singleBeds,
        int doubleBeds,
        int maxAdultCapacity,
        int maxKidsCapacity,
        String name
) {
}
