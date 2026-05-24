package com.dusanbranovic.bookme.dto.responses;

public record BookableUnitCardDTO(
        Long unitId,
        String propertyName,
        String unitName,
        String address,
        String city,
        String country,
        String imageUrl,
        int singleBeds,
        int doubleBeds,
        double totalPriceForStay
) {
}
