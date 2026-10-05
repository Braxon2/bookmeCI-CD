package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Calculates stay prices one night at a time. When price periods overlap,
 * the most recently persisted period (the one with the greatest ID) wins.
 */
@Service
public class PricingService {

    public double calculateUnitPrice(
            LocalDate start,
            LocalDate end,
            List<PeriodPrice> prices
    ) {
        validateDateRange(start, end);

        double total = 0.0;
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            total += findLatestUnitPrice(date, prices).getPricePerNight();
        }
        return total;
    }

    public double calculateAddonPrice(
            LocalDate start,
            LocalDate end,
            AddonMapping addonMapping
    ) {
        validateDateRange(start, end);

        if (!addonMapping.isPerNight()) {
            return findLatestAddonPrice(start, addonMapping.getPeriodPriceAddons()).getPrice();
        }

        double total = 0.0;
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            total += findLatestAddonPrice(date, addonMapping.getPeriodPriceAddons()).getPrice();
        }
        return total;
    }

    private PeriodPrice findLatestUnitPrice(LocalDate date, List<PeriodPrice> prices) {
        PeriodPrice selected = null;

        for (PeriodPrice candidate : prices) {
            if (covers(candidate.getStartDate(), candidate.getEndDate(), date)
                    && isNewer(candidate.getId(), selected == null ? null : selected.getId())) {
                selected = candidate;
            }
        }

        if (selected == null) {
            throw new EntityNotFoundException("No price defined for date " + date);
        }
        return selected;
    }

    private PeriodPriceAddon findLatestAddonPrice(
            LocalDate date,
            List<PeriodPriceAddon> prices
    ) {
        PeriodPriceAddon selected = null;

        for (PeriodPriceAddon candidate : prices) {
            if (covers(candidate.getStartDate(), candidate.getEndDate(), date)
                    && isNewer(candidate.getId(), selected == null ? null : selected.getId())) {
                selected = candidate;
            }
        }

        if (selected == null) {
            throw new EntityNotFoundException("No addon price defined for date " + date);
        }
        return selected;
    }

    private boolean covers(LocalDate start, LocalDate end, LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private boolean isNewer(Long candidateId, Long selectedId) {
        if (selectedId == null) {
            return true;
        }
        return candidateId != null && candidateId > selectedId;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new InvalidDateRangeException("Start date must be before end date");
        }
    }
}
