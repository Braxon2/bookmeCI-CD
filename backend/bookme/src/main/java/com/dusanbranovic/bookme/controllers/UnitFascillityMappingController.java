package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.service.UnitFascillityMappingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unit-fascility-mappings")
public class UnitFascillityMappingController {

    private final UnitFascillityMappingService unitFascillityMappingService;

    public UnitFascillityMappingController(UnitFascillityMappingService unitFascillityMappingService) {
        this.unitFascillityMappingService = unitFascillityMappingService;
    }
}
