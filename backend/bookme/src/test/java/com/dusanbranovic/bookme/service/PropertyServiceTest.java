package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.*;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PropertyMapper;
import com.dusanbranovic.bookme.mappers.PropertyTypeMapper;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyTypeRepository propertyTypeRepository;
    @Mock
    private FasiliityRepository fasiliityRepository;
    @Mock
    private PropertyFascilityRepository propertyFascilityRepository;
    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private S3Service s3Service;

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

    @Test
    void getAll_returnsMappedProperties() {
        Property p1 = new Property();
        Property p2 = new Property();

        PropertyDTO dto1 = mock(PropertyDTO.class);
        PropertyDTO dto2 = mock(PropertyDTO.class);

        when(propertyRepository.findAll()).thenReturn(List.of(p1, p2));
        when(propertyMapper.toDTO(p1)).thenReturn(dto1);
        when(propertyMapper.toDTO(p2)).thenReturn(dto2);

        List<PropertyDTO> result = propertyService.getAll();

        assertEquals(2, result.size());
        verify(propertyRepository).findAll();
    }

    @Test
    void getProperty_existingId_returnsDTO() {
        Property property = new Property();
        PropertyDTO dto = mock(PropertyDTO.class);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(propertyMapper.toDTO(property)).thenReturn(dto);

        PropertyDTO result = propertyService.getProperty(1L);

        assertNotNull(result);
    }

    @Test
    void getProperty_nonExistingId_throwsException() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> propertyService.getProperty(1L));
    }

    @Test
    void addProperty_validData_createsProperty() {
        User owner = new User();
        PropertyType type = new PropertyType(1L, "Hotel");

        Fascillity f1 = new Fascillity();
        Fascillity f2 = new Fascillity();

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(1L, "Hotel"),
                "Name",
                "Desc",
                "Country",
                "City",
                "Address",
                "Rules",
                "Info",
                List.of(
                        new FascilityResponseDTO(1L, "Wifi"),
                        new FascilityResponseDTO(2L, "Parking")
                )
        );

        Property savedProperty = new Property();
        PropertyDTO responseDTO = mock(PropertyDTO.class);

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(owner));
        when(propertyTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(fasiliityRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(f1, f2));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);
        when(propertyMapper.toDTO(savedProperty)).thenReturn(responseDTO);

        PropertyDTO result = propertyService.addProperty(dto, "test@mail.com");

        assertNotNull(result);
        verify(propertyFascilityRepository).saveAll(anyList());
    }

    @Test
    void addProperty_invalidFacilities_throwsException() {
        User owner = new User();
        PropertyType type = new PropertyType(1L, "Hotel");

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(1L, "Hotel"),
                "Name",
                "Desc",
                "Country",
                "City",
                "Address",
                "Rules",
                "Info",
                List.of(new FascilityResponseDTO(1L, "Wifi"))
        );

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(owner));
        when(propertyTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(fasiliityRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class,
                () -> propertyService.addProperty(dto, "mail@test.com"));
    }

    @Test
    void getAllUnits_returnsUnits() {
        Property property = new Property();
        BookableUnit unit = new BookableUnit();
        property.setUnits(List.of(unit));

        BookableUnitsResponseDTO dto = mock(BookableUnitsResponseDTO.class);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(bookableUnitMapper.toDTO(unit)).thenReturn(dto);

        List<BookableUnitsResponseDTO> result = propertyService.getAllUnits(1L);

        assertEquals(1, result.size());
    }

    @Test
    void addUnit_validProperty_createsUnit() {
        Property property = new Property();
        property.setUnits(new ArrayList<>());

        BookableUnitRequestDTO dto = new BookableUnitRequestDTO(
                4, 50, 2, 2, 1, 4, 1, "Room"
        );

        BookableUnit unit = new BookableUnit();
        BookableUnitsResponseDTO responseDTO = mock(BookableUnitsResponseDTO.class);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(bookableUnitMapper.toEntity(dto, property)).thenReturn(unit);
        when(bookableUnitRepository.save(unit)).thenReturn(unit);
        when(bookableUnitMapper.toDTO(unit)).thenReturn(responseDTO);

        BookableUnitsResponseDTO result = propertyService.addUnit(1L, dto);

        assertNotNull(result);
        assertEquals(1, property.getUnits().size());
    }

    @Test
    void addReview_authenticatedUser_createsReview() {
        Property property = new Property();
        User user = new User();

        ReviewRequestDTO dto = new ReviewRequestDTO(5, "Great");

        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(context);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(userMapper.toDTO(user)).thenReturn(mock(GuestSummaryDTO.class));
        when(propertyMapper.toDTO(property)).thenReturn(mock(PropertyDTO.class));

        ReviewResponseDTO result = propertyService.addReview(dto, 1L);

        assertEquals(5, result.rating());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void getReviews_returnsReviewDTOs() {
        Property property = new Property();
        Review review = new Review();
        review.setRating(4);
        property.setReviews(List.of(review));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(userMapper.toDTO(any())).thenReturn(mock(GuestSummaryDTO.class));
        when(propertyMapper.toDTO(any())).thenReturn(mock(PropertyDTO.class));

        List<ReviewResponseDTO> result = propertyService.getReviews(1L);

        assertEquals(1, result.size());
    }







}