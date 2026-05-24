package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.mappers.FascilityMapper;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FascilityService {

    private final FasiliityRepository fasiliityRepository;

    public FascilityService(FasiliityRepository fasiliityRepository) {
        this.fasiliityRepository = fasiliityRepository;
    }

    public FascilityResponseDTO addFascility(FascilityRequestDTO dto) {

        Optional<Fascillity> optionalFascillity = fasiliityRepository.findByName(dto.name());

        if(optionalFascillity.isPresent()) throw new EntityAlreadyExistsExcpetion("Fascility already exists");

        Fascillity fascillity = new Fascillity();
        fascillity.setName(dto.name());

        fasiliityRepository.save(fascillity);
        return new FascilityResponseDTO(fascillity.getId(),fascillity.getName());
    }

    public List<FascilityResponseDTO> getFascilities() {
        return fasiliityRepository.findAll()
                .stream()
                .map((fascillity) -> new FascilityResponseDTO(fascillity.getId(),fascillity.getName()))
                .collect(Collectors.toList());
    }
}
