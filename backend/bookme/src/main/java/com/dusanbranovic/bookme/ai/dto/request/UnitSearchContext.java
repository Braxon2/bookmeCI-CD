package com.dusanbranovic.bookme.ai.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;


public record UnitSearchContext(
        @Size(max = 100) String city,
        @Size(max = 100) String country,
        LocalDate checkIn,
        LocalDate checkOut,
        @Min(1) @Max(30) Integer adults,
        @Min(0) @Max(30) Integer children,
        @PositiveOrZero @DecimalMax("1000000") Double maxTotalPrice,
        @Size(max = 10) List<@NotBlank @Size(max = 100) String> propertyFacilities,
        @Size(max = 10) List<@NotBlank @Size(max = 100) String> unitFacilities
) {
}
