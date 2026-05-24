package com.dusanbranovic.bookme.dto.requests;



import java.time.LocalDate;
import java.util.List;


public record BookingRequestDTO(
        LocalDate start_date,
        LocalDate end_date,
        List<AddonsRequestDTO> addons
) {
}
