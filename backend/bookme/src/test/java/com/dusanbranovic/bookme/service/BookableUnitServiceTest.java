package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddFacilitiesRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitAddonsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitFacilitiesResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PeriodPriceResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PeriodPriceMapper;
import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.UnitFascilityMapping;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import com.dusanbranovic.bookme.repository.UnitFascillityMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookableUnitServiceTest {

    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private PeriodPriceRepository periodPriceRepository;
    @Mock
    private UnitFascilityRepository unitFascilityRepository;
    @Mock
    private UnitFascillityMappingRepository unitFascillityMappingRepository;
    @Mock
    private AddonMappingRepository addonMappingRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private BookableUnitMapper bookableUnitMapper;
    @Mock
    private PeriodPriceMapper periodPriceMapper;
    @Spy
    private PricingService pricingService = new PricingService();

    @InjectMocks
    private BookableUnitService bookableUnitService;

    private UUID unitId;
    private BookableUnit unit;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        unit = new BookableUnit();
        unit.setPublicId(unitId);
        unit.setUnitFascilityMappings(new ArrayList<>());
    }

    @Test
    void addPeriodPriceSavesAndMapsThePrice() {
        PeriodPriceRequestDTO request = new PeriodPriceRequestDTO(
                120, LocalDate.now(), LocalDate.now().plusDays(5), "Summer"
        );
        PeriodPrice price = new PeriodPrice();
        PeriodPriceResponseDTO response = org.mockito.Mockito.mock(PeriodPriceResponseDTO.class);
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(periodPriceMapper.toEntity(request, unit)).thenReturn(price);
        when(periodPriceRepository.save(price)).thenReturn(price);
        when(periodPriceMapper.toDTO(price)).thenReturn(response);

        assertSame(response, bookableUnitService.addPeriodPrice(unitId, request));
        verify(periodPriceRepository).save(price);
    }

    @Test
    void addPeriodPriceRejectsAnUnknownUnit() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> bookableUnitService.addPeriodPrice(
                        unitId,
                        new PeriodPriceRequestDTO(100, LocalDate.now(), LocalDate.now().plusDays(1), "Standard")
                )
        );
        verify(periodPriceRepository, never()).save(any());
    }

    @Test
    void getPeriodPricesMapsEveryConfiguredPrice() {
        PeriodPrice first = new PeriodPrice();
        PeriodPrice second = new PeriodPrice();
        PeriodPriceResponseDTO firstResponse = org.mockito.Mockito.mock(PeriodPriceResponseDTO.class);
        PeriodPriceResponseDTO secondResponse = org.mockito.Mockito.mock(PeriodPriceResponseDTO.class);
        unit.setPeriodPriceList(List.of(first, second));
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(periodPriceMapper.toDTO(first)).thenReturn(firstResponse);
        when(periodPriceMapper.toDTO(second)).thenReturn(secondResponse);

        assertEquals(
                List.of(firstResponse, secondResponse),
                bookableUnitService.getPeriodPrices(unitId)
        );
    }

    @Test
    void addFacilitiesReconcilesExistingAndRequestedMappings() {
        UnitFascillity oldFacility = facility(1L, "Old facility");
        UnitFascillity keptFacility = facility(2L, "Wi-Fi");
        UnitFascillity newFacility = facility(3L, "Balcony");
        UnitFascilityMapping oldMapping = new UnitFascilityMapping(unit, oldFacility);
        UnitFascilityMapping keptMapping = new UnitFascilityMapping(unit, keptFacility);
        unit.setUnitFascilityMappings(new ArrayList<>(List.of(oldMapping, keptMapping)));
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(unitFascilityRepository.findAllById(List.of(3L))).thenReturn(List.of(newFacility));

        BookableUnitFacilitiesResponseDTO result = bookableUnitService.addFacilitiesToUnit(
                unitId,
                new AddFacilitiesRequestDTO(List.of(2L, 3L, 3L))
        );

        assertEquals(unitId, result.unitId());
        assertEquals(List.of(2L, 3L), result.facilities().stream().map(item -> item.id()).toList());
        verify(unitFascillityMappingRepository).deleteAll(List.of(oldMapping));
        verify(unitFascillityMappingRepository).saveAll(anyList());
    }

    @Test
    void addFacilitiesRejectsUnknownFacilityIds() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(unitFascilityRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThrows(
                EntityNotFoundException.class,
                () -> bookableUnitService.addFacilitiesToUnit(
                        unitId,
                        new AddFacilitiesRequestDTO(List.of(99L))
                )
        );
    }

    @Test
    void getUnitAddonsCalculatesPerNightAndFlatRates() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);
        Addon breakfast = addon(4L, "Breakfast");
        Addon parking = addon(5L, "Parking");
        AddonMapping nightly = new AddonMapping(true, unit, breakfast, start.minusDays(1));
        nightly.setId(10L);
        nightly.setPeriodPriceAddons(List.of(new PeriodPriceAddon(nightly, 8, start, end)));
        AddonMapping flat = new AddonMapping(false, unit, parking, start.minusDays(1));
        flat.setId(11L);
        flat.setPeriodPriceAddons(List.of(new PeriodPriceAddon(flat, 15, start, end)));
        when(bookableUnitRepository.existsByPublicId(unitId)).thenReturn(true);
        when(addonMappingRepository.findAvailableAddonsForPeriod(unitId, start, end))
                .thenReturn(List.of(nightly, flat));

        List<BookableUnitAddonsResponseDTO> result = bookableUnitService.getUnitAddons(unitId, start, end);

        assertEquals(16, result.get(0).price());
        assertEquals(15, result.get(1).price());
    }

    @Test
    void searchUsesTheNewestPriceForEveryOverlappingNight() {
        LocalDate start = LocalDate.of(2026, 10, 9);
        LocalDate end = LocalDate.of(2026, 10, 12);
        PeriodPrice october = new PeriodPrice(
                unit, 70, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), "October"
        );
        october.setId(1L);
        PeriodPrice special = new PeriodPrice(
                unit, 100, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10), "Special"
        );
        special.setId(2L);
        unit.setPeriodPriceList(List.of(special, october));
        unit.setName("October room");

        Property property = new Property();
        property.setName("City hotel");
        property.setAddress("Main Street 1");
        property.setCity("Belgrade");
        property.setCountry("Serbia");
        property.setImages(new ArrayList<>());
        unit.setProperty(property);

        when(bookableUnitRepository.findAll(any(Specification.class))).thenReturn(List.of(unit));

        Page<com.dusanbranovic.bookme.dto.responses.BookableUnitCardDTO> result =
                bookableUnitService.searchUnits(
                        "Belgrade", "Serbia", 2, 0, start, end,
                        null, null, null, PageRequest.of(0, 20)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(270.0, result.getContent().getFirst().totalPriceForStay());
    }

    @Test
    void getUnitAddonsValidatesDateRangeBeforeAccessingTheDatabase() {
        LocalDate today = LocalDate.now();

        assertThrows(
                InvalidDateRangeException.class,
                () -> bookableUnitService.getUnitAddons(unitId, today, today)
        );
        verify(bookableUnitRepository, never()).existsByPublicId(any());
    }

    private UnitFascillity facility(Long id, String name) {
        UnitFascillity facility = new UnitFascillity(name);
        facility.setId(id);
        return facility;
    }

    private Addon addon(Long id, String name) {
        Addon addon = new Addon(name);
        addon.setId(id);
        return addon;
    }
}
