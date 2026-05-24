package com.dusanbranovic.bookme.integrations.service;
import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import com.dusanbranovic.bookme.service.FascilityService;
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
public class FascillityServiceIT {

    @Autowired
    private FasiliityRepository fasiliityRepository;

    @Autowired
    private FascilityService fascilityService;

    @Test
    @DisplayName("Should sucesfully add unit fascillity")
    void addFascility(){
        FascilityRequestDTO dto = new FascilityRequestDTO("Slave");

        FascilityResponseDTO result = fascilityService.addFascility(dto);

        assertNotNull(result);
        assertEquals(dto.name(),result.name());
    }

    @Test
    @DisplayName("Should throw EntityAlreadyExistsExcpetion")
    void addFascility_AlreadyExistExcpetion(){
        Fascillity fascillity = new Fascillity("Slave");
        fasiliityRepository.save(fascillity);
        FascilityRequestDTO dto = new FascilityRequestDTO("Slave");

        assertThrows(EntityAlreadyExistsExcpetion.class, ()->fascilityService.addFascility(dto));

    }
}
