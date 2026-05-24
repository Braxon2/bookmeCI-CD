package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import com.dusanbranovic.bookme.repository.UnitFascillityMappingRepository;
import org.springframework.stereotype.Service;

@Service
public class UnitFascillityMappingService {

    private final UnitFascillityMappingRepository unitFascillityMappingRepository;

    public UnitFascillityMappingService(UnitFascillityMappingRepository unitFascillityMappingRepository) {
        this.unitFascillityMappingRepository = unitFascillityMappingRepository;
    }
}
