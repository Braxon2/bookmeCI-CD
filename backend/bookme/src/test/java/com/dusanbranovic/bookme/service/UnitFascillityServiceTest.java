package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.requests.UnitFascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitFascillityServiceTest {

    @Mock
    private UnitFascilityRepository unitFascilityRepository;

    @InjectMocks
    private UnitFascillityService unitFascillityService;

    private UnitFascillity kitchen;
    private UnitFascillity privateBathroom;
    private UnitFascilityRequestDTO kitchenDTO;

    @BeforeEach
    void setUp() {
        privateBathroom = new UnitFascillity("Private Bathroom");
        privateBathroom.setId(1L);

        kitchen = new UnitFascillity("Kitchen");
        kitchen.setId(2L);

        kitchenDTO = new UnitFascilityRequestDTO("Kitchen");
    }
    @Test
    void addUnitFascility() {
        when(unitFascilityRepository.findByName(kitchen.getName())).thenReturn(Optional.empty());;
        UnitFascilityResponseDTO savedUnitFascility = unitFascillityService.addUnitFascility(kitchenDTO);
        assertNotNull(savedUnitFascility);
        assertEquals(kitchen.getName(),savedUnitFascility.name());
    }

    @Test
    void addUnitFascility_ThrowsEntityAlreadyExistEception() {
        when(unitFascilityRepository.findByName(kitchenDTO.name())).thenReturn(Optional.of(kitchen));;
        assertThrows(EntityAlreadyExistsExcpetion.class,() ->unitFascillityService.addUnitFascility(kitchenDTO));
    }

    @Test
    void getUnitFasilities() {
        List<UnitFascillity> fascillities = Arrays.asList(kitchen, privateBathroom);
        when(unitFascilityRepository.findAll()).thenReturn(fascillities);

        List<UnitFascilityResponseDTO> results = unitFascillityService.getUnitFasilities();

        assertNotNull(results);
        assertEquals(2, results.size());
    }
}