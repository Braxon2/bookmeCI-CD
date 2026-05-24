package com.dusanbranovic.bookme.dto.requests;

import java.time.LocalDate;

public record PeriodPriceRequestDTO(
        double pricePerNight,
        LocalDate startDate,
        LocalDate endDate,
        String season
) {
}
