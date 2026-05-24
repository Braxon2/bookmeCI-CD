package com.dusanbranovic.bookme.dto.requests;

public record BookableUnitRequestDTO(
        int maxCapacity,
        double squareMeters,
        int totalUnits,
        int singleBeds,
        int doubleBeds,
        int maxAdultCapacity,
        int maxKidsCapacity,
        String name
) {
}
