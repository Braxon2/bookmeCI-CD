package com.dusanbranovic.bookme.dto.responses;

import java.time.LocalDate;

public record PeriodPriceAddonResponseDTO(
        Long id,
        double pricePerNight,
        boolean isPerNight,
        LocalDate startDate,
        LocalDate endDate
) {
}
