package com.dusanbranovic.bookme.integrations.repository;

import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.models.UserType;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PropertyRepositoryIT {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private PropertyType type;

    private User owner;

    private Property property;

    @BeforeEach
    void setUp(){
        type = new PropertyType("Cologne");
        propertyTypeRepository.save(type);

        owner = new User();
        owner.setRole(UserType.OWNER);
        userRepository.save(owner);

        property = new Property();
        propertyRepository.save(property);

    }

    @Test
    @DisplayName("Test if the DB returns property correctly")
    void findById(){
        Long id = property.getId();
        Optional<Property> optionalProperty = propertyRepository.findById(id);

        assertTrue(optionalProperty.isPresent());
        assertEquals(id,optionalProperty.get().getId());
    }



    @Test
    void saveTest(){
        Property property = new Property();
        property.setPropertyType(type);
        property.setOwner(owner);
        property.setName("Sunny Hotel");
        property.setCity("Belgrade");
        property.setCountry("Serbia");
        property.setAddress("Main Street 1");
        property.setHouseRules("N/A");
        property.setDescription("Sunny day in Philadelphia but on Serbian");
        property.setImportantInfo("No smoking...");

        Property savedProperty = propertyRepository.save(property);

        Optional<Property> optionalProperty = propertyRepository.findById(savedProperty.getId());

        assertTrue(optionalProperty.isPresent());
        assertEquals(savedProperty.getId(), optionalProperty.get().getId());

    }

}