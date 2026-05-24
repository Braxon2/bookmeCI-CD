package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.service.PropertyFascilityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/property-fascilities")
public class PropertyFascillityController {

    private final PropertyFascilityService propertyFascilityService;

    public PropertyFascillityController(PropertyFascilityService propertyFascilityService) {
        this.propertyFascilityService = propertyFascilityService;
    }
}
