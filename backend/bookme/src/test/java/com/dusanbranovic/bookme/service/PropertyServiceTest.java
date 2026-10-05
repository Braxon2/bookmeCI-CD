package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PropertyMapper;
import com.dusanbranovic.bookme.mappers.PropertyTypeMapper;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.FasiliityRepository;
import com.dusanbranovic.bookme.repository.PropertyFascilityRepository;
import com.dusanbranovic.bookme.repository.PropertyImageRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.repository.ReviewRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyTypeRepository propertyTypeRepository;
    @Mock
    private PropertyImageRepository propertyImageRepository;
    @Mock
    private FasiliityRepository fasiliityRepository;
    @Mock
    private PropertyFascilityRepository propertyFascilityRepository;
    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PropertyMapper propertyMapper;
    @Mock
    private PropertyTypeMapper propertyTypeMapper;
    @Mock
    private BookableUnitMapper bookableUnitMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PropertyService propertyService;

    private UUID propertyPublicId;
    private Property property;
    private PropertyDTO propertyDTO;

    @BeforeEach
    void setUp() {
        property = new Property();
        propertyPublicId = property.getPublicId();
        property.setName("Riverside Hotel");

        propertyDTO = new PropertyDTO(
                propertyPublicId,
                new PropertyTypeDTO(1L, "Hotel"),
                "Riverside Hotel",
                "A comfortable hotel beside the river.",
                "Serbia",
                "Belgrade",
                "River Street 10",
                "No smoking",
                "Reception is open all day",
                List.of()
        );
    }

    @Nested
    @DisplayName("Property queries")
    class PropertyQueries {

        @Test
        @DisplayName("maps and returns every property")
        void getAllReturnsMappedProperties() {
            Property secondProperty = new Property();
            PropertyDTO secondDTO = new PropertyDTO(
                    UUID.randomUUID(), new PropertyTypeDTO(2L, "Apartment"),
                    "City Apartment", "Apartment in the city centre.", "Serbia",
                    "Novi Sad", "Main Street 5", null, null, List.of()
            );
            when(propertyRepository.findAll()).thenReturn(List.of(property, secondProperty));
            when(propertyMapper.toDTO(property)).thenReturn(propertyDTO);
            when(propertyMapper.toDTO(secondProperty)).thenReturn(secondDTO);

            List<PropertyDTO> result = propertyService.getAll();

            assertEquals(List.of(propertyDTO, secondDTO), result);
            verify(propertyRepository).findAll();
        }

        @Test
        @DisplayName("returns a property for an existing public id")
        void getPropertyReturnsMappedProperty() {
            when(propertyRepository.findByPublicId(propertyPublicId)).thenReturn(Optional.of(property));
            when(propertyMapper.toDTO(property)).thenReturn(propertyDTO);

            PropertyDTO result = propertyService.getProperty(propertyPublicId);

            assertSame(propertyDTO, result);
            verify(propertyMapper).toDTO(property);
        }

        @Test
        @DisplayName("throws when the public id does not exist")
        void getPropertyThrowsForUnknownPublicId() {
            when(propertyRepository.findByPublicId(propertyPublicId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.getProperty(propertyPublicId)
            );

            assertEquals("Property with id " + propertyPublicId + " not found", exception.getMessage());
            verify(propertyMapper, never()).toDTO(any());
        }
    }

    @Nested
    @DisplayName("Adding a property")
    class AddingProperty {

        @Test
        @DisplayName("creates the property and all selected facility mappings")
        void addPropertyCreatesPropertyAndFacilityMappings() {
            User owner = new User();
            PropertyType propertyType = new PropertyType(7L, "Villa");
            Fascillity wifi = facility(11L, "Wi-Fi");
            Fascillity parking = facility(12L, "Parking");
            PropertyRequestDTO request = propertyRequest(
                    propertyType.getId(),
                    List.of(
                            new FascilityResponseDTO(wifi.getId(), wifi.getName()),
                            new FascilityResponseDTO(parking.getId(), parking.getName())
                    )
            );

            when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
            when(propertyTypeRepository.findById(propertyType.getId())).thenReturn(Optional.of(propertyType));
            when(fasiliityRepository.findAllById(List.of(11L, 12L))).thenReturn(List.of(wifi, parking));
            when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(propertyMapper.toDTO(any(Property.class))).thenReturn(propertyDTO);

            PropertyDTO result = propertyService.addProperty(request, "owner@test.com");

            assertSame(propertyDTO, result);
            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
            verify(propertyRepository).save(propertyCaptor.capture());
            Property savedProperty = propertyCaptor.getValue();
            assertSame(owner, savedProperty.getOwner());
            assertSame(propertyType, savedProperty.getPropertyType());
            assertEquals(request.name(), savedProperty.getName());
            assertEquals(2, savedProperty.getPropertyFacilities().size());
            assertTrue(savedProperty.getPropertyFacilities().stream()
                    .allMatch(mapping -> mapping.getProperty() == savedProperty));
            verify(propertyFascilityRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("throws when the owner email does not exist")
        void addPropertyThrowsForUnknownOwner() {
            PropertyRequestDTO request = propertyRequest(7L, List.of());
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.addProperty(request, "missing@test.com")
            );

            assertEquals("User with missing@test.com not found", exception.getMessage());
            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the selected property type does not exist")
        void addPropertyThrowsForUnknownPropertyType() {
            PropertyRequestDTO request = propertyRequest(99L, List.of());
            when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(new User()));
            when(propertyTypeRepository.findById(99L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.addProperty(request, "owner@test.com")
            );

            assertEquals("Property type with 99 not found", exception.getMessage());
            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when one or more selected facilities do not exist")
        void addPropertyThrowsForInvalidFacilities() {
            Fascillity wifi = facility(11L, "Wi-Fi");
            PropertyRequestDTO request = propertyRequest(
                    7L,
                    List.of(
                            new FascilityResponseDTO(11L, "Wi-Fi"),
                            new FascilityResponseDTO(999L, "Missing facility")
                    )
            );
            when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(new User()));
            when(propertyTypeRepository.findById(7L)).thenReturn(Optional.of(new PropertyType(7L, "Villa")));
            when(fasiliityRepository.findAllById(List.of(11L, 999L))).thenReturn(List.of(wifi));

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.addProperty(request, "owner@test.com")
            );

            assertEquals("One or more fascility IDs were invalid", exception.getMessage());
            verify(propertyRepository, never()).save(any());
            verify(propertyFascilityRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Unit management")
    class UnitManagement {

        @Test
        @DisplayName("returns a mapped page of units for an existing property")
        void getAllUnitsReturnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            BookableUnit unit = new BookableUnit();
            BookableUnitsResponseDTO unitDTO = unitDTO(UUID.randomUUID(), "River room");
            when(propertyRepository.existsByPublicId(propertyPublicId)).thenReturn(true);
            when(bookableUnitRepository.findByProperty_PublicId(propertyPublicId, pageable))
                    .thenReturn(new PageImpl<>(List.of(unit), pageable, 1));
            when(bookableUnitMapper.toDTO(unit)).thenReturn(unitDTO);

            Page<BookableUnitsResponseDTO> result = propertyService.getAllUnits(propertyPublicId, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(unitDTO, result.getContent().getFirst());
        }

        @Test
        @DisplayName("throws before querying units when the property does not exist")
        void getAllUnitsThrowsForUnknownProperty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(propertyRepository.existsByPublicId(propertyPublicId)).thenReturn(false);

            assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.getAllUnits(propertyPublicId, pageable)
            );

            verify(bookableUnitRepository, never()).findByProperty_PublicId(any(), any());
        }

        @Test
        @DisplayName("creates and attaches a unit to an existing property")
        void addUnitCreatesAndAttachesUnit() {
            BookableUnitRequestDTO request = unitRequest("Deluxe room");
            BookableUnit unit = new BookableUnit();
            BookableUnitsResponseDTO response = unitDTO(UUID.randomUUID(), "Deluxe room");
            when(propertyRepository.findByPublicId(propertyPublicId)).thenReturn(Optional.of(property));
            when(bookableUnitMapper.toEntity(request, property)).thenReturn(unit);
            when(bookableUnitRepository.save(unit)).thenReturn(unit);
            when(bookableUnitMapper.toDTO(unit)).thenReturn(response);

            BookableUnitsResponseDTO result = propertyService.addUnit(propertyPublicId, request);

            assertSame(response, result);
            assertTrue(property.getUnits().contains(unit));
            verify(bookableUnitRepository).save(unit);
        }

        @Test
        @DisplayName("throws when adding a unit to an unknown property")
        void addUnitThrowsForUnknownProperty() {
            when(propertyRepository.findByPublicId(propertyPublicId)).thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.addUnit(propertyPublicId, unitRequest("Deluxe room"))
            );

            verify(bookableUnitRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Owner properties")
    class OwnerProperties {

        @Test
        @DisplayName("returns properties belonging to an existing owner")
        void getPropertiesFromOwnerReturnsMappedProperties() {
            User owner = new User();
            owner.setProperties(List.of(property));
            when(userRepository.findById(25L)).thenReturn(Optional.of(owner));
            when(propertyMapper.toDTO(property)).thenReturn(propertyDTO);

            List<PropertyDTO> result = propertyService.getPropertiesFromOwner(25L);

            assertEquals(List.of(propertyDTO), result);
        }

        @Test
        @DisplayName("throws when the owner does not exist")
        void getPropertiesFromOwnerThrowsForUnknownOwner() {
            when(userRepository.findById(404L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> propertyService.getPropertiesFromOwner(404L)
            );

            assertEquals("User with 404 ID not found", exception.getMessage());
            verify(propertyMapper, never()).toDTO(any());
        }
    }

    private PropertyRequestDTO propertyRequest(
            Long propertyTypeId,
            List<FascilityResponseDTO> facilities
    ) {
        return new PropertyRequestDTO(
                new PropertyTypeDTO(propertyTypeId, "Villa"),
                "Riverside Villa",
                "A spacious villa beside the river.",
                "Serbia",
                "Belgrade",
                "River Street 10",
                "No smoking",
                "Check-in after 14:00",
                facilities
        );
    }

    private Fascillity facility(Long id, String name) {
        Fascillity facility = new Fascillity(name);
        facility.setId(id);
        return facility;
    }

    private BookableUnitRequestDTO unitRequest(String name) {
        return new BookableUnitRequestDTO(4, 36.5, 2, 2, 1, 3, 1, name);
    }

    private BookableUnitsResponseDTO unitDTO(UUID id, String name) {
        return new BookableUnitsResponseDTO(id, 4, 36.5, 2, 1, 3, 1, name, List.of());
    }
}
