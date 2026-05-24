package com.dusanbranovic.bookme.integrations.service;
import com.dusanbranovic.bookme.dto.requests.UnitFascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import com.dusanbranovic.bookme.service.UnitFascillityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UnitFascillityServiceIT {

    @Autowired
    private UnitFascillityService unitFascillityService;

    @Autowired
    private UnitFascilityRepository unitFascilityRepository;

    @Test
    @DisplayName("Should sucessfully add unit fascility")
    void addUnitFascility(){
        UnitFascilityRequestDTO dto = new UnitFascilityRequestDTO("Klejn");

        UnitFascilityResponseDTO result = unitFascillityService.addUnitFascility(dto);
        assertNotNull(result);
        assertEquals(dto.name(),result.name());
    }

    @Test
    @DisplayName("Should throw EntityAlreadyExistsExcpetion")
    void addUnitFascility_AlreadyExistEception(){
        UnitFascillity unitFascillity = new UnitFascillity("Klejn");
        unitFascilityRepository.save(unitFascillity);
        UnitFascilityRequestDTO dto = new UnitFascilityRequestDTO("Klejn");

        assertThrows(EntityAlreadyExistsExcpetion.class,()->unitFascillityService.addUnitFascility(dto));
    }
}
