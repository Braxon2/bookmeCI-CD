package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.models.UserType;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractControllerIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected PropertyTypeRepository propertyTypeRepository;
    @Autowired protected PropertyRepository propertyRepository;
    @Autowired protected BookableUnitRepository bookableUnitRepository;
    @PersistenceContext protected EntityManager entityManager;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected User owner;
    protected User guest;
    protected PropertyType propertyType;
    protected Property property;
    protected BookableUnit unit;

    @BeforeEach
    void seedSharedData() {
        String suffix = UUID.randomUUID().toString();
        owner = userRepository.save(new User(
                UserType.OWNER,
                "owner-" + suffix + "@test.local",
                "Test",
                "Owner",
                "password",
                "0600000000"
        ));
        guest = userRepository.save(new User(
                UserType.USER,
                "guest-" + suffix + "@test.local",
                "Test",
                "Guest",
                "password",
                "0610000000"
        ));
        propertyType = propertyTypeRepository.save(new PropertyType("Hotel " + suffix));
        property = propertyRepository.save(new Property(
                owner,
                propertyType,
                "Integration property " + suffix,
                "A property used by controller integration tests.",
                "Serbia",
                "Belgrade",
                "Test Street 1",
                "No smoking",
                "Check-in after 14:00"
        ));
        unit = bookableUnitRepository.save(new BookableUnit(
                property, 4, 42.0, 2, 2, 1, 3, 1, "Integration suite"
        ));
    }

    protected RequestPostProcessor asOwner() {
        return user(owner);
    }

    protected RequestPostProcessor asGuest() {
        return user(guest);
    }

    protected RequestPostProcessor asAdmin() {
        return user("admin@test.local").authorities(() -> "ADMIN");
    }
}
