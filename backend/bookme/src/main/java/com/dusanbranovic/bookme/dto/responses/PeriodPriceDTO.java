package com.dusanbranovic.bookme.dto.responses;

import java.time.LocalDate;

public record PeriodPriceDTO(
        Long id,
        double pricePerNight,
        LocalDate startDate,
        LocalDate endDate,
        String season
) {
}
