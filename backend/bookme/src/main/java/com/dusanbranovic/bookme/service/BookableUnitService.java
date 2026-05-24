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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class BookableUnitService {

    private final BookableUnitRepository bookableUnitRepository;
    private final PeriodPriceRepository periodPriceRepository;
    private final UnitFascilityRepository unitFascilityRepository;
    private final UnitFascillityMappingRepository unitFascillityMappingRepository;

    private final BookableUnitMapper bookableUnitMapper;
    private final PeriodPriceMapper periodPriceMapper;

    private static final Logger log = LoggerFactory.getLogger(BookableUnitService.class);

    public BookableUnitService(
            BookableUnitRepository bookableUnitRepository,
            PeriodPriceRepository periodPriceRepository,
            UnitFascilityRepository unitFascilityRepository,
            UnitFascillityMappingRepository unitFascillityMappingRepository,
            BookableUnitMapper bookableUnitMapper,
            PeriodPriceMapper periodPriceMapper
    ) {
        this.bookableUnitRepository = bookableUnitRepository;
        this.periodPriceRepository = periodPriceRepository;
        this.unitFascilityRepository = unitFascilityRepository;
        this.unitFascillityMappingRepository = unitFascillityMappingRepository;
        this.bookableUnitMapper = bookableUnitMapper;
        this.periodPriceMapper = periodPriceMapper;
    }

    public PeriodPriceResponseDTO addPeriodPrice(
            Long unitId,
            PeriodPriceRequestDTO periodPriceDTO
    ) {
        BookableUnit unit = bookableUnitRepository.findById(unitId).orElseThrow(() ->{
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });


        PeriodPrice periodPrice = periodPriceMapper.toEntity(periodPriceDTO, unit);

        log.debug("Created periodPrice body {}", periodPrice);

        PeriodPrice savedPeriodPrice = periodPriceRepository.save(periodPrice);

        log.info("Period price created successfully");

        return periodPriceMapper.toDTO(savedPeriodPrice);


    }

    public List<PeriodPriceResponseDTO> getPeriodPrices(Long unitId) {

        BookableUnit unit = bookableUnitRepository.findById(unitId).orElseThrow(() ->{
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
    public List<BookableUnitCardDTO> searchUnits(
            String city,
            String country,
            int adults,
            int kids,
            LocalDate startDate,
            LocalDate endDate,
            Double maxPrice,
            List<Long> propertyFacilities,
            List<Long> unitFacilities
    ) {
        LocalDateTime checkIn = startDate.atStartOfDay();
        LocalDateTime checkOut = endDate.atStartOfDay();

        List<Long> propFacs = (propertyFacilities != null) ? propertyFacilities : List.of();
        List<Long> unitFacs = (unitFacilities != null) ? unitFacilities : List.of();

        List<BookableUnit> availableUnits = bookableUnitRepository.searchUnitsByCriteria(
                city, country, adults, kids, checkIn, checkOut, propFacs, propFacs.size(), unitFacs, unitFacs.size()
        );

        List<BookableUnitCardDTO> resultCards = new ArrayList<>();


        for (BookableUnit unit : availableUnits) {
            try {
                double totalPrice = calculatePriceForDates(startDate, endDate, unit.getPeriodPriceList());

                if (maxPrice != null && totalPrice > maxPrice) {
                    continue;
                }

                String imageUrl = null;
                List<PropertyImage> propertyImages = unit.getProperty().getImages();

                if (propertyImages != null && !propertyImages.isEmpty()) {
                    imageUrl = propertyImages.stream()
                            .filter(img -> Boolean.TRUE.equals(img.getPrimary()))
                            .map(PropertyImage::getUrl)
                            .findFirst()
                            .orElse(
                                    propertyImages.getFirst().getUrl()
                            );
                }

                resultCards.add(new BookableUnitCardDTO(
                        unit.getId(),
                        unit.getProperty().getName(),
                        unit.getName(),
                        unit.getProperty().getAddress(),
                        unit.getProperty().getCity(),
                        unit.getProperty().getCountry(),
                        imageUrl,
                        unit.getSingleBeds(),
                        unit.getDoubleBeds(),
                        totalPrice
                ));

            } catch (Exception e) {
                log.warn("Skipping unit {} from search due to pricing error: {}", unit.getId(), e.getMessage());
            }
        }

        return resultCards;
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
            Long unitId,
            AddFacilitiesRequestDTO dto
    ) {

        BookableUnit unit = bookableUnitRepository.findById(unitId).orElseThrow(() ->{
            log.error("Unit with id {} not found", unitId);
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });


        List<Long> distinctRequestedIds = dto.facilityIds().stream().distinct().toList();


        List<UnitFascillity> unitFascillityList = unitFascilityRepository.findAllById(distinctRequestedIds);

        if (unitFascillityList.size() != distinctRequestedIds.size()) {
            throw new EntityNotFoundException("One or more facilities not found in the database");
        }

        List<Long> existingFacilityIds = unit.getUnitFascilityMappings().stream()
                .map(mapping -> mapping.getUnitFascillity().getId())
                .toList();


        List<UnitFascilityMapping> newMappings = unitFascillityList.stream()
                .map(uf -> new UnitFascilityMapping(unit, uf))
                .toList();

        if (!newMappings.isEmpty()) {
            unitFascillityMappingRepository.saveAll(newMappings);
        }

        List<UnitFascilityResponseDTO> allFacilitiesDto = new ArrayList<>();

        unit.getUnitFascilityMappings().forEach(mapping ->
                allFacilitiesDto.add(new UnitFascilityResponseDTO(mapping.getUnitFascillity().getId(), mapping.getUnitFascillity().getName()))
        );

        newMappings.forEach(mapping ->
                allFacilitiesDto.add(new UnitFascilityResponseDTO(mapping.getUnitFascillity().getId(), mapping.getUnitFascillity().getName()))
        );

        return new BookableUnitFacilitiesResponseDTO(unitId, allFacilitiesDto);
    }

    public BookableUnitSummaryDTO getUnit(
            Long unitId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        BookableUnit unit = bookableUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit with ID " + unitId + " not found"));

        Property property = unit.getProperty();

        PropertyTypeDTO propertyTypeDTO = new PropertyTypeDTO(property.getPropertyType().getId(), property.getPropertyType().getName());

        List<FascilityResponseDTO> facilityDTO = property.getPropertyFacilities().stream().
                map(fac ->
                        new FascilityResponseDTO(fac.getFacility().getId(),fac.getFacility().getName()))
                .toList();

        PropertyDTO propertyDTO = new PropertyDTO(
                property.getId(),
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

        List<UnitImageDTO> unitImageDTO = unit.getImages()
                .stream().map(image ->
                        new UnitImageDTO(image.getId(), image.getUrl(), image.getPrimary(), image.getSortOrder()))
                .toList();

        double totalPrice = calculatePriceForDates(startDate, endDate, unit.getPeriodPriceList());

        log.info("Unit fetched successfully");

        return new BookableUnitSummaryDTO(
                unit.getId(),
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

    public List<BookableUnitAddonsResponseDTO> getUnitAddons(Long unitId, LocalDate startDate, LocalDate endDate) {

        BookableUnit unit = bookableUnitRepository.findById(unitId)
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

    public BookableUnitDetailedCardDTO getUnitInfo(Long unitId) {
        BookableUnit unit = bookableUnitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit with ID " + unitId + " not found"));

        Property property = unit.getProperty();

        PropertyTypeDTO propertyTypeDTO = new PropertyTypeDTO(property.getPropertyType().getId(), property.getPropertyType().getName());

        List<FascilityResponseDTO> facilityDTO = property.getPropertyFacilities().stream().
                map(fac ->
                        new FascilityResponseDTO(fac.getFacility().getId(),fac.getFacility().getName()))
                .toList();

        PropertyDTO propertyDTO = new PropertyDTO(
                property.getId(),
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

        List<UnitImageDTO> unitImageDTO = unit.getImages()
                .stream().map(image ->
                        new UnitImageDTO(image.getId(), image.getUrl(), image.getPrimary(), image.getSortOrder()))
                .toList();


        log.info("Unit fetched successfully");

        return new BookableUnitDetailedCardDTO(
                unit.getId(),
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
