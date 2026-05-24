package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.PropertyFascilityRepository;
import com.dusanbranovic.bookme.repository.PropertyImageRepository;
import org.springframework.stereotype.Service;

@Service
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;

    public PropertyImageService(PropertyImageRepository propertyImageRepository) {
        this.propertyImageRepository = propertyImageRepository;
    }
}
