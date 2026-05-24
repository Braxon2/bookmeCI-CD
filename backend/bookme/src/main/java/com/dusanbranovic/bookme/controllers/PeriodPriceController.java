package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.service.PeriodPriceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/period-prices")
public class PeriodPriceController {

    private final PeriodPriceService periodPriceService;

    public PeriodPriceController(PeriodPriceService periodPriceService) {
        this.periodPriceService = periodPriceService;
    }
}
