package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FascilityServiceTest {

    @Mock
    private FasiliityRepository fasiliityRepository;

    @InjectMocks
    private FascilityService fascilityService;

    private Fascillity gardenFascillity;
    private Fascillity poolFascillity;
    private FascilityRequestDTO gardenRequestDTO;

    @BeforeEach
    void setUp() {
        gardenFascillity = new Fascillity("Garden");
        gardenFascillity.setId(1L);

        poolFascillity = new Fascillity("Pool");
        poolFascillity.setId(2L);

        gardenRequestDTO = new FascilityRequestDTO("Garden");
    }

    @Test
    void addFascility() {
        Fascillity fascillity = new Fascillity("Garden");
        when(fasiliityRepository.findByName(fascillity.getName())).thenReturn(Optional.empty());;
        FascilityResponseDTO savedFascililty = fascilityService.addFascility(gardenRequestDTO);
        assertNotNull(savedFascililty);
        assertEquals(fascillity.getName(),savedFascililty.name());
    }

    @Test
    void addFascility_ThrowsEntityAlreadyExistEception() {
        FascilityRequestDTO dto = new FascilityRequestDTO("Garden");
        Fascillity fascillity = new Fascillity("Garden");
        when(fasiliityRepository.findByName(dto.name())).thenReturn(Optional.of(fascillity));;
        assertThrows(EntityAlreadyExistsExcpetion.class,() ->fascilityService.addFascility(dto));
    }

    @Test
    void getFascilities() {
        List<Fascillity> fascillities = Arrays.asList(gardenFascillity, poolFascillity);
        when(fasiliityRepository.findAll()).thenReturn(fascillities);

        List<FascilityResponseDTO> results = fascilityService.getFascilities();

        assertNotNull(results);
        assertEquals(2, results.size());

        FascilityResponseDTO firstResult = results.get(0);
        assertEquals(1L, firstResult.id());
        assertEquals("Garden", firstResult.name());

        FascilityResponseDTO secondResult = results.get(1);
        assertEquals(2L, secondResult.id());
        assertEquals("Pool", secondResult.name());

        verify(fasiliityRepository).findAll();
        verify(fasiliityRepository, times(1)).findAll();
    }
}