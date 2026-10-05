package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonRequestDTO;
import com.dusanbranovic.bookme.dto.requests.AddonToAddRequestDTO;
import com.dusanbranovic.bookme.dto.requests.BillingTypeRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceAddonRequestDTO;
import com.dusanbranovic.bookme.dto.responses.AddonPeriodPriceResponseDTO;
import com.dusanbranovic.bookme.dto.responses.AddonResponseDTO;
import com.dusanbranovic.bookme.dto.responses.AddonToAddResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.InvalidPriceValueException;
import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceAddonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddonServiceTest {

    @Mock
    private AddonRepository addonRepository;
    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private PeriodPriceAddonRepository periodPriceAddonRepository;
    @Mock
    private AddonMappingRepository addonMappingRepository;

    @InjectMocks
    private AddonService addonService;

    private UUID unitId;
    private BookableUnit unit;
    private Addon addon;
    private AddonMapping mapping;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        unit = new BookableUnit();
        unit.setPublicId(unitId);
        addon = new Addon("Breakfast");
        addon.setId(5L);
        mapping = new AddonMapping(false, unit, addon, LocalDate.now());
        mapping.setId(20L);
    }

    @Test
    void getAllAddonsMapsRepositoryEntities() {
        when(addonRepository.findAll()).thenReturn(List.of(addon));

        List<AddonResponseDTO> result = addonService.getAllAddons();

        assertEquals(List.of(new AddonResponseDTO(5L, "Breakfast")), result);
    }

    @Test
    void addAddonSavesAUniqueCatalogueEntry() {
        when(addonRepository.findByName("Parking")).thenReturn(Optional.empty());
        when(addonRepository.save(any(Addon.class))).thenAnswer(invocation -> {
            Addon saved = invocation.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        AddonResponseDTO result = addonService.addAddon(new AddonRequestDTO("Parking"));

        assertEquals(new AddonResponseDTO(8L, "Parking"), result);
    }

    @Test
    void addAddonRejectsDuplicateName() {
        when(addonRepository.findByName("Breakfast")).thenReturn(Optional.of(addon));

        assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> addonService.addAddon(new AddonRequestDTO("Breakfast"))
        );
        verify(addonRepository, never()).save(any());
    }

    @Test
    void addAddonToUnitCreatesAnInactivePerStayMapping() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(addonRepository.findById(5L)).thenReturn(Optional.of(addon));
        when(addonMappingRepository.findActiveByAddonAndUnit(unitId, 5L)).thenReturn(Optional.empty());
        when(addonMappingRepository.save(any(AddonMapping.class))).thenAnswer(invocation -> {
            AddonMapping saved = invocation.getArgument(0);
            saved.setId(21L);
            return saved;
        });

        AddonToAddResponseDTO result = addonService.addAddonToUnit(
                unitId,
                new AddonToAddRequestDTO(5L, "Breakfast")
        );

        assertEquals(21L, result.id());
        assertEquals("Breakfast", result.name());
        assertFalse(result.perNight());
    }

    @Test
    void addAddonToUnitRejectsAnAlreadyActiveMapping() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(addonRepository.findById(5L)).thenReturn(Optional.of(addon));
        when(addonMappingRepository.findActiveByAddonAndUnit(unitId, 5L))
                .thenReturn(Optional.of(mapping));

        assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> addonService.addAddonToUnit(unitId, new AddonToAddRequestDTO(5L, "Breakfast"))
        );
        verify(addonMappingRepository, never()).save(any());
    }

    @Test
    void addAddonPeriodPriceValidatesDatesAndPrice() {
        stubExistingMapping();
        LocalDate today = LocalDate.now();

        assertThrows(
                InvalidDateRangeException.class,
                () -> addonService.addAddonPeriodPrice(
                        unitId, 5L, new PeriodPriceAddonRequestDTO(20, today, today)
                )
        );
        assertThrows(
                InvalidPriceValueException.class,
                () -> addonService.addAddonPeriodPrice(
                        unitId, 5L, new PeriodPriceAddonRequestDTO(0, today, today.plusDays(2))
                )
        );
    }

    @Test
    void addAddonPeriodPriceCreatesPriceForActiveMapping() {
        stubExistingMapping();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        when(periodPriceAddonRepository.save(any(PeriodPriceAddon.class))).thenAnswer(invocation -> {
            PeriodPriceAddon saved = invocation.getArgument(0);
            saved.setId(31L);
            return saved;
        });

        AddonPeriodPriceResponseDTO result = addonService.addAddonPeriodPrice(
                unitId, 5L, new PeriodPriceAddonRequestDTO(15.5, start, end)
        );

        assertEquals(31L, result.id());
        assertEquals(15.5, result.price());
        assertEquals(new AddonResponseDTO(5L, "Breakfast"), result.addon());
        assertEquals(1, mapping.getPeriodPriceAddons().size());
    }

    @Test
    void changeBillingTypeUpdatesTheActiveMapping() {
        stubExistingMapping();

        boolean result = addonService.changeBillingType(unitId, 5L, new BillingTypeRequestDTO(true));

        assertTrue(result);
        assertTrue(mapping.isPerNight());
        verify(addonMappingRepository).save(mapping);
    }

    @Test
    void removeAddonFromUnitClosesTheActiveMapping() {
        when(addonMappingRepository.findActiveByAddonAndUnit(unitId, 5L))
                .thenReturn(Optional.of(mapping));

        addonService.removeAddonFromUnit(unitId, 5L);

        assertEquals(LocalDate.now(), mapping.getActiveUntil());
        verify(addonMappingRepository).save(mapping);
    }

    @Test
    void removeAddonFromUnitRejectsMissingMapping() {
        when(addonMappingRepository.findActiveByAddonAndUnit(unitId, 5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> addonService.removeAddonFromUnit(unitId, 5L));
    }

    private void stubExistingMapping() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(addonRepository.findById(5L)).thenReturn(Optional.of(addon));
        when(addonMappingRepository.findActiveByAddonAndUnit(unitId, 5L))
                .thenReturn(Optional.of(mapping));
    }
}
