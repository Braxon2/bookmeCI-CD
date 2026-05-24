package com.dusanbranovic.bookme.dto.responses;

import com.dusanbranovic.bookme.models.UserType;

public record OwnerDTO(
        Long id,
        UserType userType
) {
}
