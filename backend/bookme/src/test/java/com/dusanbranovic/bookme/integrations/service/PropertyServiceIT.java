package com.dusanbranovic.bookme.integrations.service;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.*;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PropertyMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import com.dusanbranovic.bookme.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PropertyServiceIT {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReviewRepository reviewRepository;


    @Autowired
    private FasiliityRepository fasiliityRepository;

    @Autowired
    private PropertyFascilityRepository propertyFascilityRepository;

    @Autowired
    private BookableUnitRepository bookableUnitRepository;

    @MockitoSpyBean
    private BookableUnitMapper bookableUnitMapper;

    private User owner;

    private PropertyType type;

    private BookableUnit unit;

    private Property property;

    private Fascillity fascillity1;
    private Fascillity fascillity2;



    @BeforeEach
    public void setUp() {

        owner = new User();
        owner.setRole(UserType.OWNER);
        owner.setEmail("owner4@test.com");
        owner.setFirstName("Owner");
        owner.setLastName("One");
        owner.setPassword(passwordEncoder.encode("password"));
        owner.setPhoneNumber("123456789");


        unit = new BookableUnit();
        unit.setName("Room A");
        unit.setMaxCapacity(4);
        unit.setSingleBeds(4);
        unit.setDoubleBeds(0);
        unit.setTotalUnits(2);
        unit.setMaxCapacity(4);
        unit.setMaxAdultCapacity(4);
        unit.setMaxKidsCapacity(2);
        unit.setSquareMeters(36.5);

        type = new PropertyType();
        type.setName("Beach house");

        fascillity1 = new Fascillity("Room Service");
        fascillity2 = new Fascillity("Fitness center");

        fasiliityRepository.save(fascillity1);
        fasiliityRepository.save(fascillity2);

        property = new Property();
        property.setOwner(owner);
        property.setPropertyType(type);

        List<Property> propList = new ArrayList<>();
        propList.add(property);
        owner.setProperties(propList);

        propertyTypeRepository.save(type);
        userRepository.save(owner);
        propertyRepository.save(property);
    }

    @Test
    @DisplayName("Should return a list of Properties")
    void getAllProperties(){
        List<PropertyDTO> dto = propertyService.getAll();

        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should return a Property")
    void getProperty(){
        PropertyDTO dto = propertyService.getProperty(property.getId());

        assertNotNull(dto);
    }


    @Test
    @DisplayName("Should return an Exception ENtityNotFOund")
    void getProperty_EntityNotFoundExcpetion(){
        assertThrows(EntityNotFoundException.class,() -> propertyService.getProperty(52L));
    }

    @Test
    @DisplayName("Should successfully save property and its facilities to the database")
    void addPropertyTest(){
        String email = "owner1@test.com";

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(type.getId(), type.getName()),
                "Crispy Cream",
                "Some description",
                "Serbia",
                "Belgrade",
                "Ball Street 3",
                "House rules",
                "Info",
                List.of(
                        new FascilityResponseDTO(fascillity1.getId(), fascillity1.getName()),
                        new FascilityResponseDTO(fascillity2.getId(), fascillity2.getName())
                )
        );

        PropertyDTO result = propertyService.addProperty(dto, owner.getEmail());

        assertNotNull(result);
        assertEquals("Crispy Cream", result.name());
        assertEquals("Crispy Cream", result.name());

        Property savedProperty = propertyRepository.findById(result.id()).orElseThrow();
        assertEquals(2, savedProperty.getPropertyFacilities().size());
        assertEquals("Serbia", savedProperty.getCountry());

    }

    @Test
    @DisplayName("Should throw exception EntityNotFOund for PropertyType")
    void addPropertyTest_PropertTypeNotFound(){
        String email = "owner1@test.com";

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(5L, "Hotel"),
                "Crispy Cream",
                "Some description",
                "Serbia",
                "Belgrade",
                "Ball Street 3",
                "House rules",
                "Info",
                List.of(
                        new FascilityResponseDTO(fascillity1.getId(), fascillity1.getName()),
                        new FascilityResponseDTO(fascillity2.getId(), fascillity2.getName())
                )
        );

        assertThrows(EntityNotFoundException.class,() -> propertyService.addProperty(dto, owner.getEmail()));

    }

    @Test
    @DisplayName("Should throw exception EntityNotFOund for User")
    void addPropertyTest_UserNotFound(){
        String email = "owner5@test.com";

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(1L, "Hotel"),
                "Crispy Cream",
                "Some description",
                "Serbia",
                "Belgrade",
                "Ball Street 3",
                "House rules",
                "Info",
                List.of(
                        new FascilityResponseDTO(fascillity1.getId(), fascillity1.getName()),
                        new FascilityResponseDTO(fascillity2.getId(), fascillity2.getName())
                )
        );

        assertThrows(EntityNotFoundException.class,() -> propertyService.addProperty(dto, email));

    }


    @Test
    @DisplayName("Should throw exception EntityNotFOund for Facility")
    void addPropertyTest_FacilityNotFound(){
        String email = "owner5@test.com";

        PropertyRequestDTO dto = new PropertyRequestDTO(
                new PropertyTypeDTO(1L, "Hotel"),
                "Crispy Cream",
                "Some description",
                "Serbia",
                "Belgrade",
                "Ball Street 3",
                "House rules",
                "Info",
                List.of(
                        new FascilityResponseDTO(-4L, fascillity1.getName()),
                        new FascilityResponseDTO(-9L, fascillity2.getName())
                )
        );

        assertThrows(EntityNotFoundException.class,() -> propertyService.addProperty(dto, owner.getEmail()));

    }

    @Test
    @DisplayName("Should successfully save a unit to a property")
    void getAllUnitsFromProperty(){
        List<BookableUnitsResponseDTO> dto = propertyService.getAllUnits(property.getId());
        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should throw exception EntityNotFOund for Property")
    void getAllUnitsFromProperty_PropertyNotFound(){
        assertThrows(EntityNotFoundException.class, () -> propertyService.getAllUnits(5L));
    }

    @Test
    @DisplayName("Should successfully save a unit to a property")
    void addUnit(){
        Optional<Property> optionalProperty = propertyRepository.findById(property.getId());
        int numberOfUnits = optionalProperty.get().getUnits().size();

        BookableUnitRequestDTO dto = new BookableUnitRequestDTO(
                unit.getMaxCapacity(),
                unit.getSquareMeters(),
                unit.getTotalUnits(),
                unit.getSingleBeds(),
                unit.getDoubleBeds(),
                unit.getMaxAdultCapacity(),
                unit.getMaxKidsCapacity(),
                unit.getName()
        );

        BookableUnit bookableUnit = bookableUnitMapper.toEntity(dto,optionalProperty.get());


        BookableUnit savedUnit = bookableUnitRepository.save(bookableUnit);
        List<BookableUnit> bookableUnits = optionalProperty.get().getUnits();
        bookableUnits.add(savedUnit);

        assertNotNull(savedUnit);
        assertEquals(numberOfUnits+1,bookableUnits.size());
    }

    @Test
    @DisplayName("Should throw exception EntityNotFOund for Property ")
    void addUnit_PropertyNotFound(){
        BookableUnitRequestDTO dto = new BookableUnitRequestDTO(
                unit.getMaxCapacity(),
                unit.getSquareMeters(),
                unit.getTotalUnits(),
                unit.getSingleBeds(),
                unit.getDoubleBeds(),
                unit.getMaxAdultCapacity(),
                unit.getMaxKidsCapacity(),
                unit.getName()
        );

        assertThrows(EntityNotFoundException.class, () -> propertyService.addUnit(5L,dto));
    }

    @Test
    @DisplayName("Should successfully add a review")
    void addReview() {
        PropertyType type = propertyTypeRepository.save(new PropertyType("Lumberjack House"));

        Property property = new Property();
        property.setOwner(owner);
        property.setPropertyType(type);
        property.setName("Review Target");
        property = propertyRepository.save(property);

        User guest = new User();
        guest.setEmail("guest@test.com");
        userRepository.save(guest);

        Authentication auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewRequestDTO dto = new ReviewRequestDTO(5,"Amazing!");

        ReviewResponseDTO response = propertyService.addReview(dto, property.getId());

        assertNotNull(response);
        assertEquals("Amazing!", response.text());
        assertEquals(guest.getEmail(), response.reviewer().email());
    }

    @Test
    @DisplayName("Should throw exception EntityNotFOund for Property")
    void getAllReviewsFromProperty_PropertyNotFound(){
        assertThrows(EntityNotFoundException.class, () -> propertyService.getAllUnits(5L));
    }

    @Test
    @DisplayName("Should fetch all reviews for a specific property")
    void getReviews() {

        User guest = new User();
        guest.setEmail("guest_reviewer@test.com");
        guest = userRepository.save(guest);

        Review r1 = new Review();
        r1.setText("Great stay!");
        r1.setRating(5);
        r1.setReviewer(guest);
        r1.setProperty(property);
        r1.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(r1);

        Review r2 = new Review();
        r2.setText("Good, but noisy.");
        r2.setRating(3);
        r2.setReviewer(guest);
        r2.setProperty(property);
        r2.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(r2);

        List<Review> list = new ArrayList<>();
        list.add(r1);
        list.add(r2);

        property.setReviews(list);
        propertyRepository.save(property);

        List<ReviewResponseDTO> reviews = propertyService.getReviews(property.getId());

        assertNotNull(reviews);
        assertEquals(2, reviews.size());
    }

    @Test
    @DisplayName("Should fetch all properties belonging to a specific owner")
    void getPropertiesFromOwner() {
        List<PropertyDTO> properties = propertyService.getPropertiesFromOwner(owner.getId());

        assertNotNull(properties);
        assertFalse(properties.isEmpty());
        assertEquals(property.getId(), properties.get(0).id());
    }

    @Test
    @DisplayName("Should throw Exception when fetching properties for an unknown owner")
    void getPropertiesFromOwner_UserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> propertyService.getPropertiesFromOwner(999L));
    }

    @Test
    @DisplayName("Should fetch property image URLs")
    void getPropertyImages() {
        PropertyImage img1 = new PropertyImage();
        img1.setUrl("http://example.com/image1.jpg");
        img1.setProperty(property);

        PropertyImage img2 = new PropertyImage();
        img2.setUrl("http://example.com/image2.jpg");
        img2.setProperty(property);

        List<PropertyImage> listImg = new ArrayList<>();
        listImg.add(img1);
        listImg.add(img2);

        property.setImages(listImg);
        propertyRepository.save(property);

        List<String> images = propertyService.getPropertyImages(property.getId());

        assertNotNull(images);
        assertEquals(2, images.size());
        assertTrue(images.contains("http://example.com/image1.jpg"));
    }

    @Test
    @DisplayName("Should throw Exception when fetching images for an unknown property")
    void getPropertyImages_PropertyNotFound() {
        assertThrows(EntityNotFoundException.class, () -> propertyService.getPropertyImages(999L));
    }

    @Test
    @DisplayName("Should fetch the primary thumbnail URL for a property")
    void getThumbnail() {
        PropertyImage primaryImg = new PropertyImage();
        primaryImg.setUrl("http://example.com/primary.jpg");
        primaryImg.setPrimary(true);
        primaryImg.setProperty(property);

        PropertyImage secondaryImg = new PropertyImage();
        secondaryImg.setUrl("http://example.com/secondary.jpg");
        secondaryImg.setPrimary(false);
        secondaryImg.setProperty(property);

        List<PropertyImage> listImg = new ArrayList<>();
        listImg.add(primaryImg);
        listImg.add(secondaryImg);
        property.setImages(listImg);
        propertyRepository.save(property);

        String thumbnail = propertyService.getThumbnail(property.getId());

        assertNotNull(thumbnail);
        assertEquals("http://example.com/primary.jpg", thumbnail);
    }

    @Test
    @DisplayName("Should throw Exception when fetching thumbnail for an unknown property")
    void getThumbnail_PropertyNotFound() {
        assertThrows(EntityNotFoundException.class, () -> propertyService.getThumbnail(999L));
    }



}
