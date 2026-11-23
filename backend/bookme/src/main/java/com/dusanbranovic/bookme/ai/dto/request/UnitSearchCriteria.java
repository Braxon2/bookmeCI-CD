package com.dusanbranovic.bookme.ai.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.ai.tool.annotation.ToolParam;
import java.time.LocalDate;
import java.util.List;

public record UnitSearchCriteria(
        @NotBlank @Size(max = 100) @ToolParam(description = "City as stored in BookMe; required") String city,
        @NotBlank @Size(max = 100) @ToolParam(description = "Country as stored in BookMe; required") String country,
        @NotNull @ToolParam(description = "Check-in ISO date YYYY-MM-DD, with explicit year") LocalDate checkIn,
        @NotNull @ToolParam(description = "Exclusive check-out ISO date YYYY-MM-DD, after checkIn") LocalDate checkOut,
        @NotNull @Min(1) @Max(30) @ToolParam(description = "Number of adults, at least 1") Integer adults,
        @NotNull @Min(0) @Max(30) @ToolParam(description = "Number of children; zero when none requested") Integer children,
        @PositiveOrZero @DecimalMax("1000000") @ToolParam(required = false, description = "Maximum TOTAL stay price, never nightly; null means no limit") Double maxTotalPrice,
        @Size(max = 10) @ToolParam(required = false, description = "Exact property facility names from the supplied catalog; ALL must match") List<@NotBlank @Size(max = 100) String> propertyFacilities,
        @Size(max = 10) @ToolParam(required = false, description = "Exact unit facility names from the supplied catalog; ALL must match") List<@NotBlank @Size(max = 100) String> unitFacilities
) {
}
