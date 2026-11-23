package com.dusanbranovic.bookme.ai.dto.response;

import java.util.List;

public record FacilityCatalog(
        List<String> propertyFacilities,
        List<String> unitFacilities,
        boolean truncated
) {
}
