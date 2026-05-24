package com.dusanbranovic.bookme.dto.responses;

import java.time.LocalDate;

public record AddonPeriodPriceResponseDTO(
        Long id,
        double price,
        LocalDate startDate,
        LocalDate endDate,
        AddonResponseDTO addon
) {
}
