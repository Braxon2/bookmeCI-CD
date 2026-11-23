package com.dusanbranovic.bookme.ai.dto.response;

import java.time.LocalDate;
import java.util.List;

public record UnitSearchFilters(String city, String country, LocalDate startDate, LocalDate endDate,
                                int adults, int kids, Double maxPrice,
                                List<Long> propertyFacilities, List<Long> unitFacilities) {
}
