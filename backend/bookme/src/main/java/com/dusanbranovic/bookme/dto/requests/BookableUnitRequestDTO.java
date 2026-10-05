package com.dusanbranovic.bookme.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookableUnitRequestDTO(

        @Min(value = 1, message = "Maximum capacity must be at least 1")
        int maxCapacity,

        @DecimalMin(value = "1.0", message = "Unit size must be at least 1 square metre")
        double squareMeters,

        @Min(value = 1, message = "Number of units must be at least 1")
        int totalUnits,

        @Min(value = 0, message = "Number of single beds cannot be negative")
        int singleBeds,

        @Min(value = 0, message = "Number of double beds cannot be negative")
        int doubleBeds,

        @Min(value = 1, message = "Maximum adult capacity must be at least 1")
        int maxAdultCapacity,

        @Min(value = 0, message = "Maximum children capacity cannot be negative")
        int maxKidsCapacity,

        @NotBlank(message = "Unit name is required")
        @Size(min = 2, max = 100, message = "Unit name must be between 2 and 100 characters")
        String name
) {
}
