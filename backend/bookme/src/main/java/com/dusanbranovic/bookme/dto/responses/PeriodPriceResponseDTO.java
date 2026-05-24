package com.dusanbranovic.bookme.dto.responses;

import java.time.LocalDate;

public record PeriodPriceResponseDTO(
        Long id,
        BookableUnitsResponseDTO bookableUnitsResponseDTO,
        double pricePerNight,
        LocalDate startDate,
        LocalDate endDate,
        String season
) {
}
