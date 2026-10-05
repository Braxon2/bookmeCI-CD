package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.models.UserType;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PropertyControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private FasiliityRepository fasiliityRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BookableUnitRepository bookableUnitRepository;

    private User owner;
    private PropertyType propertyType;
    private Fascillity facility;
    private Property existingProperty;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = UUID.randomUUID().toString();

        owner = userRepository.save(new User(
                UserType.OWNER,
                "controller-owner-" + uniqueSuffix + "@test.local",
                "Test",
                "Owner",
                "not-used-by-mock-security",
                "0600000000"
        ));
        propertyType = propertyTypeRepository.save(new PropertyType("Controller type " + uniqueSuffix));
        facility = fasiliityRepository.save(new Fascillity("Controller facility " + uniqueSuffix));
        existingProperty = propertyRepository.save(new Property(
                owner,
                propertyType,
                "Existing Riverside Property",
                "A property created for the controller integration test.",
                "Serbia",
                "Belgrade",
                "River Street 20",
                "No smoking",
                "Check-in after 14:00"
        ));
    }

    @Test
    @DisplayName("GET /api/properties/{id} returns a persisted property")
    void getPropertyReturnsPersistedProperty() throws Exception {
        mockMvc.perform(get("/api/properties/{propertyPublicId}", existingProperty.getPublicId())
                        .with(ownerUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(existingProperty.getPublicId().toString()))
                .andExpect(jsonPath("$.name").value("Existing Riverside Property"))
                .andExpect(jsonPath("$.city").value("Belgrade"))
                .andExpect(jsonPath("$.propertyTypeDTO.id").value(propertyType.getId()));
    }

    @Test
    @DisplayName("GET /api/properties/{id} returns 404 for an unknown public id")
    void getPropertyReturnsNotFoundForUnknownPublicId() throws Exception {
        UUID missingPublicId = UUID.randomUUID();

        mockMvc.perform(get("/api/properties/{propertyPublicId}", missingPublicId)
                        .with(ownerUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Property with id " + missingPublicId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/properties/" + missingPublicId));
    }

    @Test
    @DisplayName("POST /api/properties persists a property and its facilities")
    void addPropertyPersistsPropertyAndFacilities() throws Exception {
        PropertyRequestDTO request = propertyRequest("New Mountain Lodge");

        MvcResult mvcResult = mockMvc.perform(post("/api/properties")
                        .with(ownerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Mountain Lodge"))
                .andExpect(jsonPath("$.propertyTypeDTO.id").value(propertyType.getId()))
                .andExpect(jsonPath("$.fascilitiesDTO[0].id").value(facility.getId()))
                .andReturn();

        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        UUID createdPublicId = UUID.fromString(response.get("publicId").asText());
        Property createdProperty = propertyRepository.findByPublicId(createdPublicId).orElseThrow();

        assertEquals(owner.getId(), createdProperty.getOwner().getId());
        assertEquals(propertyType.getId(), createdProperty.getPropertyType().getId());
        assertEquals(1, createdProperty.getPropertyFacilities().size());
        assertEquals(facility.getId(), createdProperty.getPropertyFacilities().getFirst().getFacility().getId());
    }

    @Test
    @DisplayName("POST /api/properties rejects an invalid request body")
    void addPropertyRejectsInvalidRequest() throws Exception {
        PropertyRequestDTO invalidRequest = new PropertyRequestDTO(
                new PropertyTypeDTO(propertyType.getId(), propertyType.getName()),
                "",
                "short",
                "Serbia",
                "Belgrade",
                "River Street 10",
                null,
                null,
                List.of()
        );

        mockMvc.perform(post("/api/properties")
                        .with(ownerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/properties/{id}/units returns persisted units as a page")
    void getAllUnitsReturnsPersistedUnits() throws Exception {
        BookableUnit unit = new BookableUnit(
                existingProperty, 4, 42.5, 2, 2, 1, 3, 1, "Family suite"
        );
        unit = bookableUnitRepository.save(unit);

        mockMvc.perform(get("/api/properties/{pid}/units", existingProperty.getPublicId())
                        .param("page", "0")
                        .param("size", "10")
                        .with(ownerUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(unit.getPublicId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Family suite"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/properties/{id}/add-unit persists a valid unit")
    void addUnitPersistsValidUnit() throws Exception {
        BookableUnitRequestDTO request = new BookableUnitRequestDTO(
                4, 36.5, 2, 2, 1, 3, 1, "Deluxe river room"
        );

        MvcResult mvcResult = mockMvc.perform(post(
                                "/api/properties/{pid}/add-unit",
                                existingProperty.getPublicId()
                        )
                        .with(ownerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Deluxe river room"))
                .andExpect(jsonPath("$.maxCapacity").value(4))
                .andReturn();

        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        UUID unitPublicId = UUID.fromString(response.get("id").asText());
        BookableUnit createdUnit = bookableUnitRepository.findByPublicId(unitPublicId).orElseThrow();

        assertEquals(existingProperty.getId(), createdUnit.getProperty().getId());
        assertEquals("Deluxe river room", createdUnit.getName());
    }

    @Test
    @DisplayName("POST /api/properties is forbidden for a regular user")
    void addPropertyIsForbiddenForRegularUser() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .with(user("guest@test.local").authorities(new SimpleGrantedAuthority("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyRequest("Forbidden property"))))
                .andExpect(status().isForbidden());

        assertTrue(propertyRepository.findAll().stream()
                .noneMatch(property -> "Forbidden property".equals(property.getName())));
    }

    private PropertyRequestDTO propertyRequest(String name) {
        return new PropertyRequestDTO(
                new PropertyTypeDTO(propertyType.getId(), propertyType.getName()),
                name,
                "A complete description for the new test property.",
                "Serbia",
                "Zlatibor",
                "Mountain Road 15",
                "No parties",
                "Reception closes at 22:00",
                List.of(new FascilityResponseDTO(facility.getId(), facility.getName()))
        );
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor ownerUser() {
        return user(owner.getEmail()).authorities(new SimpleGrantedAuthority("OWNER"));
    }
}
