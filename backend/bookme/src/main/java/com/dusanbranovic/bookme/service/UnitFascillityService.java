package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.UnitFascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnitFascillityService {

    private final UnitFascilityRepository unitFascilityRepository;

    public UnitFascillityService(UnitFascilityRepository unitFascilityRepository) {
        this.unitFascilityRepository = unitFascilityRepository;
    }

    public UnitFascilityResponseDTO addUnitFascility(UnitFascilityRequestDTO dto) {
        Optional<UnitFascillity> optionalFascillity = unitFascilityRepository.findByName(dto.name());

        if(optionalFascillity.isPresent()) throw new EntityAlreadyExistsExcpetion("Unit fascility already exists");

        UnitFascillity unitFascillity = new UnitFascillity();
        unitFascillity.setName(dto.name());

        unitFascilityRepository.save(unitFascillity);
        return new UnitFascilityResponseDTO(unitFascillity.getId(),unitFascillity.getName());
    }

    public List<UnitFascilityResponseDTO> getUnitFasilities() {
        return unitFascilityRepository.findAll()
                .stream()
                .map((uf)->
                        new UnitFascilityResponseDTO(uf.getId(),uf.getName())
                )
                .collect(Collectors.toList());
    }
}
