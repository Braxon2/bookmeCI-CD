package com.dusanbranovic.bookme.dto.responses;

import java.time.LocalDateTime;

public record ReviewResponseDTO(
        Long id,
        int rating,
        String text,
        GuestSummaryDTO reviewer,
        PropertyDTO property,
        LocalDateTime createdAt
) {
}
