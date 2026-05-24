package com.dusanbranovic.bookme.dto.responses;

public record AddonToAddResponseDTO(
        Long id,
        String name,
        boolean perNight
) {
}
