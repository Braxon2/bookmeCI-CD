package com.dusanbranovic.bookme.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PeriodPriceRequestDTO(
        @Positive(message = "Price per night must be greater than zero")
        double pricePerNight,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "Season name cannot be empty")
        @Size(
                max = 100,
                message = "Season name cannot exceed 100 characters"
        )
        String season
) {
}
