package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchCriteria;
import com.dusanbranovic.bookme.ai.dto.response.FacilityCatalog;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchFilters;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.service.FascilityService;
import com.dusanbranovic.bookme.service.UnitFascillityService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class UnitSearchFilterService {
    private final FascilityService propertyFacilities;
    private final UnitFascillityService unitFacilities;
    private final Validator validator;

    public UnitSearchFilterService(FascilityService propertyFacilities, UnitFascillityService unitFacilities, Validator validator) {
        this.propertyFacilities = propertyFacilities;
        this.unitFacilities = unitFacilities;
        this.validator = validator;
    }

    public FacilityCatalog catalog() {
        var properties = propertyFacilities.getFascilities();
        var units = unitFacilities.getUnitFasilities();
        return new FacilityCatalog(properties.stream().limit(100).map(FascilityResponseDTO::name).toList(),
                units.stream().limit(100).map(UnitFascilityResponseDTO::name).toList(),
                properties.size() > 100 || units.size() > 100);
    }

    public UnitSearchFilters resolve(UnitSearchCriteria criteria) {
        if (criteria == null || !validator.validate(criteria).isEmpty()) {
            throw UnitSearchException.invalid("Provide valid city, country, dates, guests and supported filters.");
        }
        if (!criteria.checkIn().isBefore(criteria.checkOut())
                || ChronoUnit.DAYS.between(criteria.checkIn(), criteria.checkOut()) > 90) {
            throw UnitSearchException.invalid("Check-out must follow check-in; stays are limited to 90 nights.");
        }
        if (criteria.maxTotalPrice() != null && !Double.isFinite(criteria.maxTotalPrice())) {
            throw UnitSearchException.invalid("Maximum total price must be finite.");
        }
        var propertyIds = criteria.propertyFacilities() == null || criteria.propertyFacilities().isEmpty() ? List.<Long>of()
                : resolveNames(criteria.propertyFacilities(), propertyFacilities.getFascilities(), FascilityResponseDTO::name, FascilityResponseDTO::id);
        var unitIds = criteria.unitFacilities() == null || criteria.unitFacilities().isEmpty() ? List.<Long>of()
                : resolveNames(criteria.unitFacilities(), unitFacilities.getUnitFasilities(), UnitFascilityResponseDTO::name, UnitFascilityResponseDTO::id);
        return new UnitSearchFilters(criteria.city().trim(), criteria.country().trim(), criteria.checkIn(), criteria.checkOut(),
                criteria.adults(), criteria.children(), criteria.maxTotalPrice(), propertyIds, unitIds);
    }

    private <T> List<Long> resolveNames(List<String> names, List<T> catalog, Function<T, String> name, Function<T, Long> id) {
        return names.stream().map(value -> {
            var matches = catalog.stream().filter(item -> name.apply(item).equalsIgnoreCase(value.trim())).map(id).distinct().toList();
            if (matches.size() != 1) throw UnitSearchException.invalid("Unknown or ambiguous facility. Use an exact name from the supplied catalog.");
            return matches.getFirst();
        }).distinct().toList();
    }
}
