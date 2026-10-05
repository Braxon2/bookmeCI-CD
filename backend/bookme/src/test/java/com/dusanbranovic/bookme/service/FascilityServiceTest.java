package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FascilityServiceTest {

    @Mock private FasiliityRepository fascilityRepository;
    @InjectMocks private FascilityService fascilityService;

    @Test
    void addFacilityPersistsAndReturnsANewFacility() {
        when(fascilityRepository.findByName("Garden")).thenReturn(Optional.empty());
        when(fascilityRepository.save(any(Fascillity.class))).thenAnswer(invocation -> {
            Fascillity saved = invocation.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        FascilityResponseDTO result = fascilityService.addFascility(new FascilityRequestDTO("Garden"));

        assertEquals(8L, result.id());
        assertEquals("Garden", result.name());
        ArgumentCaptor<Fascillity> captor = ArgumentCaptor.forClass(Fascillity.class);
        verify(fascilityRepository).save(captor.capture());
        assertEquals("Garden", captor.getValue().getName());
    }

    @Test
    void addFacilityRejectsADuplicateName() {
        when(fascilityRepository.findByName("Garden"))
                .thenReturn(Optional.of(new Fascillity("Garden")));

        assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> fascilityService.addFascility(new FascilityRequestDTO("Garden"))
        );
        verify(fascilityRepository, never()).save(any());
    }

    @Test
    void getFacilitiesMapsAllStoredFacilities() {
        Fascillity garden = new Fascillity("Garden");
        garden.setId(1L);
        Fascillity pool = new Fascillity("Pool");
        pool.setId(2L);
        when(fascilityRepository.findAll()).thenReturn(List.of(garden, pool));

        List<FascilityResponseDTO> result = fascilityService.getFascilities();

        assertEquals(List.of("Garden", "Pool"), result.stream().map(FascilityResponseDTO::name).toList());
        assertEquals(List.of(1L, 2L), result.stream().map(FascilityResponseDTO::id).toList());
    }
}
