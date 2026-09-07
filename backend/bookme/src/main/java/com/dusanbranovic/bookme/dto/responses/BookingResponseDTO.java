package com.dusanbranovic.bookme.dto.responses;

import com.dusanbranovic.bookme.models.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponseDTO(
        UUID id,
        BookableUnitsResponseDTO bookableUnit,
        GuestSummaryDTO guest,
        Double totalPrice,
        LocalDate createdAt,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        BookingStatus status
) {
}
