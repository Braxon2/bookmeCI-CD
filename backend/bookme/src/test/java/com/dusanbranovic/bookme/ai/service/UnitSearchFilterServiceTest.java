package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchCriteria;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.service.FascilityService;
import com.dusanbranovic.bookme.service.UnitFascillityService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitSearchFilterServiceTest {
    private final FascilityService properties = mock(FascilityService.class);
    private final UnitFascillityService units = mock(UnitFascillityService.class);
    private final LocalDate start = LocalDate.of(2026, 12, 10), end = start.plusDays(5);
    private ValidatorFactory validation;
    private UnitSearchFilterService service;

    @BeforeEach void setup() {
        validation = Validation.buildDefaultValidatorFactory();
        service = new UnitSearchFilterService(properties, units, validation.getValidator());
    }
    @AfterEach void close() { validation.close(); }

    @Test void mapsExactFacilityNamesToRealIdsAndRetainsTotalPriceDatesAndCapacity() {
        when(properties.getFascilities()).thenReturn(List.of(new FascilityResponseDTO(7L, "Parking")));
        when(units.getUnitFasilities()).thenReturn(List.of(new UnitFascilityResponseDTO(8L, "Wi-Fi")));
        var filters = service.resolve(new UnitSearchCriteria(" Belgrade ", " Serbia ", start, end, 2, 1, 300.0,
                List.of(" parking ", "Parking"), List.of("Wi-Fi")));
        assertEquals("Belgrade", filters.city());
        assertEquals(start, filters.startDate());
        assertEquals(end, filters.endDate());
        assertEquals(2, filters.adults());
        assertEquals(1, filters.kids());
        assertEquals(300, filters.maxPrice());
        assertEquals(List.of(7L), filters.propertyFacilities());
        assertEquals(List.of(8L), filters.unitFacilities());
    }

    @Test void optionalFiltersProduceEmptyListsWithoutLookingUpCatalogs() {
        var filters = service.resolve(new UnitSearchCriteria("Belgrade", "Serbia", start, end, 1, 0, null, null, null));
        assertNull(filters.maxPrice());
        assertTrue(filters.propertyFacilities().isEmpty());
        verifyNoInteractions(properties, units);
    }

    @Test void invalidCriteriaNeverReachCatalogServices() {
        var invalid = List.of(
                new UnitSearchCriteria(" ", "Serbia", start, end, 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", null, start, end, 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", null, end, 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", end, start, 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, start, 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, start.plusDays(91), 2, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, end, 0, 0, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, end, 2, -1, null, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, end, 2, 0, -1.0, null, null),
                new UnitSearchCriteria("Belgrade", "Serbia", start, end, 2, 0, Double.NaN, null, null));
        invalid.forEach(value -> assertThrows(UnitSearchException.class, () -> service.resolve(value)));
        assertThrows(UnitSearchException.class, () -> service.resolve(null));
        verifyNoInteractions(properties, units);
    }

    @Test void inventedFacilitiesAreRejectedInsteadOfSilentlyDroppingRequirements() {
        when(properties.getFascilities()).thenReturn(List.of(new FascilityResponseDTO(7L, "Parking")));
        assertThrows(UnitSearchException.class, () -> service.resolve(new UnitSearchCriteria("Belgrade", "Serbia", start, end, 2, 0, null, List.of("Sauna"), null)));
    }

    @Test void catalogHasNamesOnlyAndIsBoundedForTheModel() {
        when(properties.getFascilities()).thenReturn(java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> new FascilityResponseDTO((long)i, "Facility " + i)).toList());
        when(units.getUnitFasilities()).thenReturn(List.of(new UnitFascilityResponseDTO(9L, "Wi-Fi")));
        var catalog = service.catalog();
        assertEquals(100, catalog.propertyFacilities().size());
        assertEquals(List.of("Wi-Fi"), catalog.unitFacilities());
        assertTrue(catalog.truncated());
    }
}
