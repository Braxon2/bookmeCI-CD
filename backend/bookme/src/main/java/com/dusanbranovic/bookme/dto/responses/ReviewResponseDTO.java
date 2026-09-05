package com.dusanbranovic.bookme.dto.responses;

import com.dusanbranovic.bookme.dto.requests.ReviewAuthorDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDTO(
        UUID publicId,
        int rating,
        String text,
        ReviewAuthorDTO reviewer,
        UUID bookableUnitPublicId,
        String bookableUnitName,
        LocalDateTime createdAt
) {}
