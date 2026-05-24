package com.dusanbranovic.bookme.dto.requests;

import java.time.LocalDate;

public record PeriodPriceAddonRequestDTO(
         double price,
         LocalDate startDate,
         LocalDate endDate
) {
}
