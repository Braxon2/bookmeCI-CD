package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PropertyMapper;
import com.dusanbranovic.bookme.mappers.PropertyTypeMapper;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final FasiliityRepository fasiliityRepository;
    private final PropertyFascilityRepository propertyFascilityRepository;


    private final PropertyMapper propertyMapper;
    private final PropertyTypeMapper propertyTypeMapper;
    private final BookableUnitMapper bookableUnitMapper;
    private final BookableUnitRepository bookableUnitRepository;
    private final ReviewRepository reviewRepository;
    private final UserMapper userMapper;


    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    public PropertyService(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            PropertyTypeRepository propertyTypeRepository,
            FasiliityRepository fasiliityRepository,
            PropertyFascilityRepository propertyFascilityRepository,
            PropertyMapper propertyMapper,
            PropertyTypeMapper propertyTypeMapper,
            BookableUnitMapper bookableUnitMapper,
            BookableUnitRepository bookableUnitRepository,
            ReviewRepository reviewRepository,
            UserMapper userMapper
    ) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.propertyTypeRepository = propertyTypeRepository;
        this.fasiliityRepository = fasiliityRepository;
        this.propertyFascilityRepository = propertyFascilityRepository;
        this.propertyMapper = propertyMapper;
        this.propertyTypeMapper = propertyTypeMapper;
        this.bookableUnitMapper = bookableUnitMapper;
        this.bookableUnitRepository = bookableUnitRepository;
        this.reviewRepository = reviewRepository;
        this.userMapper = userMapper;
    }

    public List<PropertyDTO> getAll() {
        log.info("Propertes successfully fetched");
        return propertyRepository.findAll()
                .stream()
                .map(propertyMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PropertyDTO getProperty(Long pid) {

        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        log.info("Property successfully fetched");

        return propertyMapper.toDTO(
                    property
        );
    }

    public PropertyDTO addProperty(PropertyRequestDTO dto, String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    log.error("User not found");
                    return new EntityNotFoundException("User with " + email + " not found");
        }
        );

        PropertyType propertyType = propertyTypeRepository.findById(dto.propertyTypeDTO().id())
                .orElseThrow(() ->{
                    log.error("Property type not found");
                    return new EntityNotFoundException("Property type with " + dto.propertyTypeDTO().id() + " not found");
                    }
        );


        List<Long> fascilityIds = dto.fascilitiesDTO().
                stream().map(FascilityResponseDTO::id).
                collect(Collectors.toList()
                );

        List<Fascillity> fascillities = fasiliityRepository.findAllById(fascilityIds);

        if(fascillities.size() != fascilityIds.size()){
            log.error("One or more fascility IDs were invalid");
            throw new EntityNotFoundException("One or more fascility IDs were invalid");
        }

        Property property = new Property();
        property.setOwner(owner);
        property.setPropertyType(propertyType);
        property.setName(dto.name());
        property.setDescription(dto.description());
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setHouseRules(dto.houseRules());
        property.setImportantInfo(dto.importantInfo());

        log.debug("Created property body {}", property);


        Property savedProperty = propertyRepository.save(property);
        List<PropertyFacility> propertyFacilities = fascillities.
                stream().
                map(fascillity -> new PropertyFacility(savedProperty,fascillity)).
                collect(Collectors.toList());

        propertyFascilityRepository.saveAll(propertyFacilities);
        savedProperty.setPropertyFacilities(propertyFacilities);

        log.info("Property created successfully");

        return propertyMapper.toDTO(savedProperty);
    }


    public List<BookableUnitsResponseDTO> getAllUnits(Long pid) {
        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        if(property.getUnits().isEmpty()) log.warn("Property have no units");
        else log.info("Units successfully fetched");

        return property.getUnits().
                stream().
                map(bookableUnitMapper::toDTO)
                .collect(Collectors.toList());

    }

    public BookableUnitsResponseDTO addUnit(Long pid, BookableUnitRequestDTO dto) {

        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        BookableUnit unit = bookableUnitMapper.toEntity(dto,property);

        log.debug("Created unit body {}", unit);

        BookableUnit savedUnit = bookableUnitRepository.save(unit);
        List<BookableUnit> units = property.getUnits();
        units.add(unit);

        log.info("Unit created successfully");

        return bookableUnitMapper.toDTO(savedUnit);

    }

    public ReviewResponseDTO addReview(ReviewRequestDTO dto, Long pid) {

        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User guest = (User) auth.getPrincipal();

        Review review = new Review();
        review.setReviewer(guest);
        review.setProperty(property);
        review.setText(dto.text());
        review.setRating(dto.rating());
        review.setCreatedAt(LocalDateTime.now());

        log.debug("Created review body {}", review);


        reviewRepository.save(review);


        log.info("Review created successfully");

        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getText(),
                userMapper.toDTO(review.getReviewer()),
                propertyMapper.toDTO(review.getProperty()),
                review.getCreatedAt());
    }

    public List<ReviewResponseDTO> getReviews(Long pid) {

        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        log.info("Reviews successfully fetched");

        return property.getReviews().
                stream().
                map(review ->
                        new ReviewResponseDTO(review.getId(),
                review.getRating(),
                review.getText(),
                userMapper.toDTO(review.getReviewer()),
                propertyMapper.toDTO(review.getProperty()),
                review.getCreatedAt()
                        )
                ).collect(Collectors.toList());
    }


    public List<PropertyDTO> getPropertiesFromOwner(Long userID) {
        User owner = userRepository.findById(userID)
                .orElseThrow(() ->{
                            log.error("User not found");
                            return new EntityNotFoundException("User with " + userID + " ID not found");
                        }
                );
        return owner.getProperties()
                .stream()
                .map(propertyMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getPropertyImages(Long pid) {
        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        log.info("Property images successfully fetched");

        return property.getImages().
                stream()
                .map(PropertyImage::getUrl)
                .collect(Collectors.toList());
    }

    public String getThumbnail(Long pid) {
        Property property = propertyRepository.findById(pid).orElseThrow(() ->{
            log.error("Property not found");
            return new EntityNotFoundException("Property with id " + pid + " not found");
        });

        String thumbnailUrl =  property.getImages().
                stream()
                .filter(PropertyImage::getPrimary)
                .findFirst()
                .map(PropertyImage::getUrl)
                .orElse(null);


        if (thumbnailUrl != null) {
            log.info("Property thumbnail image successfully fetched" + thumbnailUrl);
        } else {
            log.warn("No primary image found for property id: {}", pid);
        }

        return thumbnailUrl;
    }
}
