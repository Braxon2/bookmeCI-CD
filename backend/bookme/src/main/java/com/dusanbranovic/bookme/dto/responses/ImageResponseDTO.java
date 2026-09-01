package com.dusanbranovic.bookme.dto.responses;

public record ImageResponseDTO(
        Long id,
        String url,
        boolean primary,
        int sortOrder
) {
}
