package com.dusanbranovic.bookme.dto.responses;

public record GuestSummaryDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
