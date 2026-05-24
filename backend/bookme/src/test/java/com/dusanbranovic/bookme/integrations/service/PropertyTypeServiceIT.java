package com.dusanbranovic.bookme.integrations.service;
import com.dusanbranovic.bookme.dto.requests.PropertyTypeRequestDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.service.PropertyTypeService;
import org.junit.jupiter.api.BeforeEach;
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
public class PropertyTypeServiceIT {

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyTypeService propertyTypeService;

    @BeforeEach
    public void setup(){
        propertyTypeRepository.save(new PropertyType("Hotel"));
    }

    @Test
    @DisplayName("Should sucessfully save property type")
    void addPropertyType(){
        PropertyTypeRequestDTO dto = new PropertyTypeRequestDTO("Sandbox");

        PropertyType result = propertyTypeService.addType(dto);

        assertNotNull(result);
        assertEquals("Sandbox",result.getName());

    }

    @Test
    @DisplayName("Should throw EntityAlreadyExist")
    void addPropertyType_AlreadyExist(){
        PropertyTypeRequestDTO dto = new PropertyTypeRequestDTO("Hotel");
        assertThrows(EntityAlreadyExistsExcpetion.class, ()->propertyTypeService.addType(dto));

    }
}
