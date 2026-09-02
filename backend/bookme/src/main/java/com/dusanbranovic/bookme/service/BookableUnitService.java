package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddFacilitiesRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceRequestDTO;
import com.dusanbranovic.bookme.dto.responses.*;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PeriodPriceMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import com.dusanbranovic.bookme.repository.UnitFascillityMappingRepository;
import com.dusanbranovic.bookme.specifications.BookableUnitSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class BookableUnitService {

    private final BookableUnitRepository bookableUnitRepository;
    private final PeriodPriceRepository periodPriceRepository;
    private final UnitFascilityRepository unitFascilityRepository;
    private final UnitFascillityMappingRepository unitFascillityMappingRepository;
    private final S3Service s3Service;

    private final BookableUnitMapper bookableUnitMapper;
    private final PeriodPriceMapper periodPriceMapper;

    private static final Logger log = LoggerFactory.getLogger(BookableUnitService.class);

    public BookableUnitService(
            BookableUnitRepository bookableUnitRepository,
            PeriodPriceRepository periodPriceRepository,
            UnitFascilityRepository unitFascilityRepository,
            UnitFascillityMappingRepository unitFascillityMappingRepository,
            S3Service s3Service,
            BookableUnitMapper bookableUnitMapper,
            PeriodPriceMapper periodPriceMapper
    ) {
        this.bookableUnitRepository = bookableUnitRepository;
        this.periodPriceRepository = periodPriceRepository;
        this.unitFascilityRepository = unitFascilityRepository;
        this.unitFascillityMappingRepository = unitFascillityMappingRepository;
        this.s3Service = s3Service;
        this.bookableUnitMapper = bookableUnitMapper;
        this.periodPriceMapper = periodPriceMapper;
    }

    public PeriodPriceResponseDTO addPeriodPrice(
            UUID unitId,
            PeriodPriceRequestDTO periodPriceDTO
    ) {
        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() ->{
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });


        PeriodPrice periodPrice = periodPriceMapper.toEntity(periodPriceDTO, unit);

        log.debug("Created periodPrice body {}", periodPrice);

        PeriodPrice savedPeriodPrice = periodPriceRepository.save(periodPrice);

        log.info("Period price created successfully");

        return periodPriceMapper.toDTO(savedPeriodPrice);


    }

    public List<PeriodPriceResponseDTO> getPeriodPrices(UUID unitId) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() ->{
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        log.info("Period price fetched successfully");

        return unit.getPeriodPriceList().
                stream().
                map(periodPriceMapper::toDTO).
                collect(toList()
                );
    }

    @Transactional(readOnly = true)
    public Page<BookableUnitCardDTO> searchUnits(
            String city, String country, int adults, int kids,
            LocalDate startDate, LocalDate endDate, Double maxPrice,
            List<Long> propertyFacilities, List<Long> unitFacilities,
            Pageable pageable
    ) {
        LocalDateTime checkIn = startDate.atStartOfDay();
        LocalDateTime checkOut = endDate.atStartOfDay();

        Specification<BookableUnit> spec = Specification.where(BookableUnitSpecification.inLocation(city, country))
                .and(BookableUnitSpecification.hasCapacity(adults, kids))
                .and(BookableUnitSpecification.isAvailable(checkIn, checkOut))
                .and(BookableUnitSpecification.hasPropertyFacilities(propertyFacilities))
                .and(BookableUnitSpecification.hasUnitFacilities(unitFacilities));

        List<BookableUnit> availableUnits = bookableUnitRepository.findAll(spec);

        List<BookableUnitCardDTO> resultCards = new ArrayList<>();
        for (BookableUnit unit : availableUnits) {
            try {
                double totalPrice = calculatePriceForDates(startDate, endDate, unit.getPeriodPriceList());
                if (maxPrice != null && totalPrice > maxPrice) continue;

                String imageUrl = null;
                List<PropertyImage> propertyImages = unit.getProperty().getImages();
                if (propertyImages != null && !propertyImages.isEmpty()) {
                    String s3Key = propertyImages.stream()
                            .filter(img -> Boolean.TRUE.equals(img.getPrimary()))
                            .map(PropertyImage::getS3Key)
                            .findFirst()
                            .orElse(propertyImages.get(0).getS3Key());
                    imageUrl = s3Service.createPresignedGetUrl(s3Key);
                }

                resultCards.add(new BookableUnitCardDTO(
                        unit.getPublicId(), unit.getProperty().getName(), unit.getName(),
                        unit.getProperty().getAddress(), unit.getProperty().getCity(),
                        unit.getProperty().getCountry(), imageUrl,
                        unit.getSingleBeds(), unit.getDoubleBeds(), totalPrice
                ));
            } catch (Exception e) {
                log.warn("Skipping unit {} from search due to pricing error: {}", unit.getId(), e.getMessage());
            }
        }

        int start = (int) pageable.getOffset();
        if (start >= resultCards.size()) {
            return new PageImpl<>(List.of(), pageable, resultCards.size());
        }
        int end = Math.min(start + pageable.getPageSize(), resultCards.size());
        return new PageImpl<>(resultCards.subList(start, end), pageable, resultCards.size());
    }

    private double calculatePriceForDates(LocalDate start, LocalDate end, List<PeriodPrice> prices) {
        double totalPrice = 0.0;

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;

            PeriodPrice priceForDay = prices.stream()
                    .filter(p -> !finalDate.isBefore(p.getStartDate()) && !finalDate.isAfter(p.getEndDate()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No price defined for date " + finalDate));

            totalPrice += priceForDay.getPricePerNight();
        }
        return totalPrice;
    }

    @Transactional
    public BookableUnitFacilitiesResponseDTO addFacilitiesToUnit(
            UUID unitId,
            AddFacilitiesRequestDTO dto
    ) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() -> {
            log.error("Unit with id {} not found", unitId);
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        List<Long> requestedIds = dto.facilityIds().stream().distinct().toList();

        // 1. Get currently saved mappings
        List<UnitFascilityMapping> existingMappings = unit.getUnitFascilityMappings();
        List<Long> existingIds = existingMappings.stream()
                .map(mapping -> mapping.getUnitFascillity().getId())
                .toList();

        // 2. Determine which mappings to REMOVE (unchecked on frontend)
        List<UnitFascilityMapping> mappingsToRemove = existingMappings.stream()
                .filter(mapping -> !requestedIds.contains(mapping.getUnitFascillity().getId()))
                .toList();

        if (!mappingsToRemove.isEmpty()) {
            unitFascillityMappingRepository.deleteAll(mappingsToRemove);
            existingMappings.removeAll(mappingsToRemove); // Update in-memory list
        }

        // 3. Determine which IDs to ADD (checked on frontend, not yet in DB)
        List<Long> idsToAdd = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!idsToAdd.isEmpty()) {
            List<UnitFascillity> facilitiesToAdd = unitFascilityRepository.findAllById(idsToAdd);

            if (facilitiesToAdd.size() != idsToAdd.size()) {
                throw new EntityNotFoundException("One or more facilities not found in the database");
            }

            List<UnitFascilityMapping> newMappings = facilitiesToAdd.stream()
                    .map(uf -> new UnitFascilityMapping(unit, uf))
                    .toList();

            unitFascillityMappingRepository.saveAll(newMappings);
            existingMappings.addAll(newMappings); // Update in-memory list
        }

        // 4. Build response from the updated existingMappings list
        List<UnitFascilityResponseDTO> allFacilitiesDto = existingMappings.stream()
                .map(mapping -> new UnitFascilityResponseDTO(
                        mapping.getUnitFascillity().getId(),
                        mapping.getUnitFascillity().getName()
                ))
                .toList();

        return new BookableUnitFacilitiesResponseDTO(unitId, allFacilitiesDto);
    }


    public BookableUnitSummaryDTO getUnit(
            UUID unitId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit with ID " + unitId + " not found"));

        Property property = unit.getProperty();

        PropertyTypeDTO propertyTypeDTO = new PropertyTypeDTO(property.getPropertyType().getId(), property.getPropertyType().getName());

        List<FascilityResponseDTO> facilityDTO = property.getPropertyFacilities().stream().
                map(fac ->
                        new FascilityResponseDTO(fac.getFacility().getId(),fac.getFacility().getName()))
                .toList();

        PropertyDTO propertyDTO = new PropertyDTO(
                property.getPublicId(),
                propertyTypeDTO,
                property.getName(),
                property.getDescription(),
                property.getCountry(),
                property.getCity(),
                property.getAddress(),
                property.getHouseRules(),
                property.getImportantInfo(),
                facilityDTO);

        List<PeriodPriceDTO> periodPriceDTO = unit.getPeriodPriceList()
                .stream().map(price ->
                        new PeriodPriceDTO(price.getId(),price.getPricePerNight(),price.getStartDate(),price.getEndDate(),price.getSeason()))
                .toList();

        List<AddonResponseDTO> addonDTO = unit.getAddonMappings()
                .stream().map(addon ->
                        new AddonResponseDTO(addon.getAddon().getId(), addon.getAddon().getName()))
                .toList();

        List<UnitFascilityResponseDTO> unitFacilityDTO = unit.getUnitFascilityMappings()
                .stream().map(ufac ->
                        new UnitFascilityResponseDTO(ufac.getUnitFascillity().getId(),ufac.getUnitFascillity().getName()))
                .toList();

        List<ImageResponseDTO> unitImageDTO = s3Service.getUnitImages(unitId);

        double totalPrice = calculatePriceForDates(startDate, endDate, unit.getPeriodPriceList());

        log.info("Unit fetched successfully");

        return new BookableUnitSummaryDTO(
                unit.getPublicId(),
                propertyDTO,
                unitFacilityDTO,
                unitImageDTO,
                unit.getMaxCapacity(),
                unit.getSquareMeters(),
                unit.getSingleBeds(),
                unit.getDoubleBeds(),
                unit.getMaxAdultCapacity(),
                unit.getMaxKidsCapacity(),
                unit.getName(),
                totalPrice
        );
    }


    public List<BookableUnitAddonsResponseDTO> getUnitAddons(UUID unitId, LocalDate startDate, LocalDate endDate) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit with ID " + unitId + " not found"));

        List<AddonMapping> addonMappings = unit.getAddonMappings();

        return addonMappings.stream()
                .map(mapping -> {
                    return new BookableUnitAddonsResponseDTO(
                            mapping.getId(),
                            mapping.getAddon().getId(),
                            mapping.getAddon().getName(),
                            calculateAddonPrice(startDate,endDate,mapping),
                            mapping.isPerNight()
                    );
                }).collect(Collectors.toList());
    }

    private double calculateAddonPrice(
            LocalDate start,
            LocalDate end,
            AddonMapping addonMapping
    ) {
        List<PeriodPriceAddon> prices = addonMapping.getPeriodPriceAddons();

        if (addonMapping.isPerNight()) {
            return calculatePerNight(start, end, prices);
        } else {
            return calculateOnce(start, prices);
        }
    }

    private double calculateOnce(
            LocalDate start,
            List<PeriodPriceAddon> prices
    ) {
        PeriodPriceAddon price = prices.stream()
                .filter(p ->
                        !start.isBefore(p.getStartDate()) &&
                                !start.isAfter(p.getEndDate())
                )
                .reduce((firsy, second) -> second)
                .orElseThrow(() -> {
                    log.warn("No addon price defined");
                    return new EntityNotFoundException("No addon price defined");
                });

        return price.getPrice();
    }

    private double calculatePerNight(
            LocalDate start,
            LocalDate end,
            List<PeriodPriceAddon> prices
    ) {

        double totalPrice = 0.0;
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {

            LocalDate finalDate = date;
            PeriodPriceAddon priceForDay = prices.stream()
                    .filter(p ->
                            !finalDate.isBefore(p.getStartDate()) &&
                                    !finalDate.isAfter(p.getEndDate())
                    )
                    .findFirst()
                    .orElseThrow(() -> {
                                log.warn("No price defined for date " + finalDate);
                                return new EntityNotFoundException(
                                        "No price defined for date " + finalDate);
                            }
                    );

            totalPrice += priceForDay.getPrice();
        }

        return totalPrice;
    }


    public BookableUnitDetailedCardDTO getUnitInfo(UUID unitId) {
        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit with ID " + unitId + " not found"));

        Property property = unit.getProperty();

        PropertyTypeDTO propertyTypeDTO = new PropertyTypeDTO(property.getPropertyType().getId(), property.getPropertyType().getName());

        List<FascilityResponseDTO> facilityDTO = property.getPropertyFacilities().stream().
                map(fac ->
                        new FascilityResponseDTO(fac.getFacility().getId(),fac.getFacility().getName()))
                .toList();

        PropertyDTO propertyDTO = new PropertyDTO(
                property.getPublicId(),
                propertyTypeDTO,
                property.getName(),
                property.getDescription(),
                property.getCountry(),
                property.getCity(),
                property.getAddress(),
                property.getHouseRules(),
                property.getImportantInfo(),
                facilityDTO);

        List<PeriodPriceDTO> periodPriceDTO = unit.getPeriodPriceList()
                .stream().map(price ->
                        new PeriodPriceDTO(price.getId(),price.getPricePerNight(),price.getStartDate(),price.getEndDate(),price.getSeason()))
                .toList();

        List<AddonResponseDTO> addonDTO = unit.getAddonMappings()
                .stream().map(addon ->
                        new AddonResponseDTO(addon.getAddon().getId(), addon.getAddon().getName()))
                .toList();

        List<UnitFascilityResponseDTO> unitFacilityDTO = unit.getUnitFascilityMappings()
                .stream().map(ufac ->
                        new UnitFascilityResponseDTO(ufac.getUnitFascillity().getId(),ufac.getUnitFascillity().getName()))
                .toList();

        List<ImageResponseDTO> unitImageDTO = s3Service.getUnitImages(unitId);


        log.info("Unit fetched successfully");

        return new BookableUnitDetailedCardDTO(
                unit.getPublicId(),
                propertyDTO,
                periodPriceDTO,
                addonDTO,
                unitFacilityDTO,
                unitImageDTO,
                unit.getMaxCapacity(),
                unit.getSquareMeters(),
                unit.getSingleBeds(),
                unit.getDoubleBeds(),
                unit.getMaxAdultCapacity(),
                unit.getMaxKidsCapacity(),
                unit.getName()
        );
    }

}
