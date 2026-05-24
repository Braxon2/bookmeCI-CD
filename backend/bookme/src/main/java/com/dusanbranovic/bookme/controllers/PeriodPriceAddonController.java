package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.service.PeriodPriceAddonService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/period-prices-addons")
public class PeriodPriceAddonController {

    private final PeriodPriceAddonService periodPriceAddonService;

    public PeriodPriceAddonController(PeriodPriceAddonService periodPriceAddonService) {
        this.periodPriceAddonService = periodPriceAddonService;
    }
}
