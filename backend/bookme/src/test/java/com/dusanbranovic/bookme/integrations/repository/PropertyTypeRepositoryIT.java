package com.dusanbranovic.bookme.integrations.repository;

import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PropertyTypeRepositoryIT {

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    private PropertyType type;

    @BeforeEach
    public void setup(){
        type = new PropertyType();
        type.setName("Hotel");
        propertyTypeRepository.save(type);
    }

    @Test
    @DisplayName("Testing if the database is saving new property type")
    void savePropertyType(){
        PropertyType propertyType = new PropertyType();
        propertyType.setName("Studio");

        propertyTypeRepository.save(propertyType);

        Optional<PropertyType> propertyTypeOptional = propertyTypeRepository.findByName(propertyType.getName());

        assertTrue(propertyTypeOptional.isPresent());
        assertEquals(propertyTypeOptional.get().getName(), propertyType.getName());

    }

    @Test
    @DisplayName("Testing if the database is finding property type by name")
    void findByName(){
        Optional<PropertyType> propertyTypeOptional = propertyTypeRepository.findByName("Hotel");

        assertTrue(propertyTypeOptional.isPresent());
        assertEquals("Hotel",propertyTypeOptional.get().getName());

    }
}