package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.UnitImageRepository;
import org.springframework.stereotype.Service;

@Service
public class UnitImageService {

    private final UnitImageRepository unitImageRepository;

    public UnitImageService(UnitImageRepository unitImageRepository) {
        this.unitImageRepository = unitImageRepository;
    }
}
