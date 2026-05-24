package com.dusanbranovic.bookme.dto.requests;

import java.util.List;

public record AddFacilitiesRequestDTO(
        List<Long> facilityIds
) {
}
