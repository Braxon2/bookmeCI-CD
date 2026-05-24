package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.PeriodPriceAddonRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import org.springframework.stereotype.Service;

@Service
public class PeriodPriceService {

    private final PeriodPriceRepository periodPriceRepository;

    public PeriodPriceService(PeriodPriceRepository periodPriceRepository) {
        this.periodPriceRepository = periodPriceRepository;
    }
}
