package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.FasiliityRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceAddonRepository;
import org.springframework.stereotype.Service;

@Service
public class PeriodPriceAddonService {

    private final PeriodPriceAddonRepository periodPriceAddonRepository;

    public PeriodPriceAddonService(PeriodPriceAddonRepository periodPriceAddonRepository) {
        this.periodPriceAddonRepository = periodPriceAddonRepository;
    }
}
