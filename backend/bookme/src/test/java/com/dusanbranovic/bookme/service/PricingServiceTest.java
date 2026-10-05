package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();
    private final BookableUnit unit = new BookableUnit();

    @Test
    void unitPriceUsesNewerPeriodsAsDailyOverrides() {
        PeriodPrice october = unitPrice(1L, 70, date(1), date(31));
        PeriodPrice special = unitPrice(2L, 100, date(8), date(10));

        double total = pricingService.calculateUnitPrice(
                date(9), date(12), List.of(special, october)
        );

        assertEquals(270.0, total);
    }

    @Test
    void perNightAddonUsesTheSameDailyOverrideRule() {
        AddonMapping mapping = new AddonMapping();
        mapping.setPerNight(true);
        PeriodPriceAddon october = addonPrice(mapping, 1L, 10, date(1), date(31));
        PeriodPriceAddon special = addonPrice(mapping, 2L, 25, date(8), date(10));
        mapping.setPeriodPriceAddons(List.of(special, october));

        double total = pricingService.calculateAddonPrice(date(9), date(12), mapping);

        assertEquals(60.0, total);
    }

    @Test
    void flatAddonUsesTheNewestPriceValidOnCheckInDate() {
        AddonMapping mapping = new AddonMapping();
        mapping.setPerNight(false);
        PeriodPriceAddon october = addonPrice(mapping, 1L, 15, date(1), date(31));
        PeriodPriceAddon special = addonPrice(mapping, 2L, 30, date(8), date(10));
        mapping.setPeriodPriceAddons(List.of(october, special));

        assertEquals(30.0, pricingService.calculateAddonPrice(date(9), date(12), mapping));
    }

    @Test
    void unitPriceRequiresCoverageForEveryNight() {
        PeriodPrice limited = unitPrice(1L, 70, date(9), date(10));

        assertThrows(
                EntityNotFoundException.class,
                () -> pricingService.calculateUnitPrice(date(9), date(12), List.of(limited))
        );
    }

    @Test
    void calculationRejectsAnInvalidStayRange() {
        assertThrows(
                InvalidDateRangeException.class,
                () -> pricingService.calculateUnitPrice(date(9), date(9), List.of())
        );
    }

    private PeriodPrice unitPrice(
            Long id,
            double price,
            LocalDate start,
            LocalDate end
    ) {
        PeriodPrice periodPrice = new PeriodPrice(unit, price, start, end, "Rate");
        periodPrice.setId(id);
        return periodPrice;
    }

    private PeriodPriceAddon addonPrice(
            AddonMapping mapping,
            Long id,
            double price,
            LocalDate start,
            LocalDate end
    ) {
        PeriodPriceAddon periodPrice = new PeriodPriceAddon(mapping, price, start, end);
        periodPrice.setId(id);
        return periodPrice;
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 10, day);
    }
}
