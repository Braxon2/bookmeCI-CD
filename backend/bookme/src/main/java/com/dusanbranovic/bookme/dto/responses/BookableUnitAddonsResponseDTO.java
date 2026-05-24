package com.dusanbranovic.bookme.dto.responses;

public record BookableUnitAddonsResponseDTO(
        Long id,
        Long addonID,
        String name,
        double price,
        boolean perNight
) {
}
