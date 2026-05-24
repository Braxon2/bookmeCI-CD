package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import com.dusanbranovic.bookme.repository.PropertyFascilityRepository;
import org.springframework.stereotype.Service;

@Service
public class PropertyFascilityService {

    private final PropertyFascilityRepository propertyFascilityRepository;

    public PropertyFascilityService(PropertyFascilityRepository propertyFascilityRepository) {
        this.propertyFascilityRepository = propertyFascilityRepository;
    }
}
