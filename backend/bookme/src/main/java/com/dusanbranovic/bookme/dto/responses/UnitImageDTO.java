package com.dusanbranovic.bookme.dto.responses;

public record UnitImageDTO(
        Long id,
        String url,
        Boolean isPrimary,
        Integer sortOrder
) {
}
