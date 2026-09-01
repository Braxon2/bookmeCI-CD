package com.dusanbranovic.bookme.dto.responses;

import java.util.UUID;

public record BookableUnitCardDTO(
        UUID unitId,
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
