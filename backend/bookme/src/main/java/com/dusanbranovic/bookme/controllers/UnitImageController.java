package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.service.UnitImageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unit-images")
public class UnitImageController {

    private final UnitImageService unitImageService;

    public UnitImageController(UnitImageService unitImageService) {
        this.unitImageService = unitImageService;
    }
}
