package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.UnitFascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
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
class UnitFascillityServiceTest {

    @Mock private UnitFascilityRepository unitFascilityRepository;
    @InjectMocks private UnitFascillityService unitFascillityService;

    @Test
    void addUnitFacilityPersistsAndReturnsANewFacility() {
        when(unitFascilityRepository.findByName("Kitchen")).thenReturn(Optional.empty());
        when(unitFascilityRepository.save(any(UnitFascillity.class))).thenAnswer(invocation -> {
            UnitFascillity saved = invocation.getArgument(0);
            saved.setId(6L);
            return saved;
        });

        UnitFascilityResponseDTO result = unitFascillityService.addUnitFascility(
                new UnitFascilityRequestDTO("Kitchen")
        );

        assertEquals(6L, result.id());
        assertEquals("Kitchen", result.name());
        ArgumentCaptor<UnitFascillity> captor = ArgumentCaptor.forClass(UnitFascillity.class);
        verify(unitFascilityRepository).save(captor.capture());
        assertEquals("Kitchen", captor.getValue().getName());
    }

    @Test
    void addUnitFacilityRejectsADuplicateName() {
        when(unitFascilityRepository.findByName("Kitchen"))
                .thenReturn(Optional.of(new UnitFascillity("Kitchen")));

        assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> unitFascillityService.addUnitFascility(new UnitFascilityRequestDTO("Kitchen"))
        );
        verify(unitFascilityRepository, never()).save(any());
    }

    @Test
    void getUnitFacilitiesMapsAllStoredFacilities() {
        UnitFascillity kitchen = new UnitFascillity("Kitchen");
        kitchen.setId(1L);
        UnitFascillity bathroom = new UnitFascillity("Private bathroom");
        bathroom.setId(2L);
        when(unitFascilityRepository.findAll()).thenReturn(List.of(kitchen, bathroom));

        List<UnitFascilityResponseDTO> result = unitFascillityService.getUnitFasilities();

        assertEquals(
                List.of("Kitchen", "Private bathroom"),
                result.stream().map(UnitFascilityResponseDTO::name).toList()
        );
        assertEquals(List.of(1L, 2L), result.stream().map(UnitFascilityResponseDTO::id).toList());
    }
}
