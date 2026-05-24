package com.dusanbranovic.bookme.integrations.service;

import com.dusanbranovic.bookme.dto.requests.AddFacilitiesRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitCardDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitFacilitiesResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PeriodPriceResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import com.dusanbranovic.bookme.service.BookableUnitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookableUnitServiceIT {

    @Autowired
    private BookableUnitRepository bookableUnitRepository;

    @Autowired
    private PeriodPriceRepository periodPriceRepository;

    @Autowired
    private BookableUnitService bookableUnitService;

    @Autowired
    private UnitFascilityRepository unitFascilityRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private FasiliityRepository fasiliityRepository;

    @Autowired
    private UnitFascillityMappingRepository unitFascillityMappingRepository;

    @Autowired
    private PropertyFascilityRepository propertyFascilityRepository;

    @Test
    @DisplayName("Should sucesfully save a period price")
    void addPeriodPrice(){

        BookableUnit testUnit = new BookableUnit();
        bookableUnitRepository.save(testUnit);
        LocalDate start = LocalDate.of(2026,1,1);
        LocalDate end = LocalDate.of(2026,3,8);
        PeriodPriceRequestDTO dto = new PeriodPriceRequestDTO(34,start,end,"winter");

        PeriodPriceResponseDTO result = bookableUnitService.addPeriodPrice(testUnit.getId(),dto);

        assertNotNull(result);
        Optional<BookableUnit> optionalBookableUnit = bookableUnitRepository.findById(result.bookableUnitsResponseDTO().id());
        assertEquals(testUnit.getId(),optionalBookableUnit.get().getId());
    }

    @Test
    @DisplayName("Should throw Exception for Unit not found")
    void addPeriodPrice_UnitNotFound(){

        LocalDate start = LocalDate.of(2026,1,1);
        LocalDate end = LocalDate.of(2026,3,8);
        PeriodPriceRequestDTO dto = new PeriodPriceRequestDTO(34.50,start,end,"winter");

        assertThrows(EntityNotFoundException.class, () -> bookableUnitService.addPeriodPrice(10L,dto));
    }

    @Test
    @DisplayName("Should fetch a list of period prices for a valid Unit")
    void getPeriodPrices() {
        BookableUnit testUnit = new BookableUnit();
        testUnit = bookableUnitRepository.save(testUnit);

        PeriodPrice price1 = new PeriodPrice();
        price1.setBookableUnit(testUnit);
        price1.setPricePerNight(50.0);
        periodPriceRepository.save(price1);

        PeriodPrice price2 = new PeriodPrice();
        price2.setBookableUnit(testUnit);
        price2.setPricePerNight(75.0);
        periodPriceRepository.save(price2);

        List<PeriodPrice> list = new ArrayList<>();
        list.add(price1);
        list.add(price2);
        testUnit.setPeriodPriceList(list);
        bookableUnitRepository.save(testUnit);

        List<PeriodPriceResponseDTO> results = bookableUnitService.getPeriodPrices(testUnit.getId());

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should throw Exception when fetching period prices for an unknown Unit")
    void getPeriodPrices_UnitNotFound() {
        assertThrows(EntityNotFoundException.class, () -> bookableUnitService.getPeriodPrices(999L));
    }

    @Test
    @DisplayName("Should successfully add facilities to a unit")
    void addFacilitiesToUnit() {
        BookableUnit testUnit = new BookableUnit();
        testUnit = bookableUnitRepository.save(testUnit);

        UnitFascillity facility1 = new UnitFascillity();
        facility1.setName("Wi-Fi");
        facility1 = unitFascilityRepository.save(facility1);

        UnitFascillity facility2 = new UnitFascillity();
        facility2.setName("Air Conditioning");
        facility2 = unitFascilityRepository.save(facility2);

        AddFacilitiesRequestDTO dto = new AddFacilitiesRequestDTO(List.of(facility1.getId(), facility2.getId()));

        BookableUnitFacilitiesResponseDTO response = bookableUnitService.addFacilitiesToUnit(testUnit.getId(), dto);

        assertNotNull(response);
        assertEquals(testUnit.getId(), response.unitId());
        assertEquals(2, response.facilities().size());
    }

    @Test
    @DisplayName("Should throw Exception when adding facilities to an unknown Unit")
    void addFacilitiesToUnit_UnitNotFound() {
        AddFacilitiesRequestDTO dto = new AddFacilitiesRequestDTO(List.of(1L));
        assertThrows(EntityNotFoundException.class, () -> bookableUnitService.addFacilitiesToUnit(999L, dto));
    }

    @Test
    @DisplayName("Should throw Exception when adding non-existent facilities to a Unit")
    void addFacilitiesToUnit_FacilityNotFound() {
        BookableUnit testUnit = new BookableUnit();
        testUnit = bookableUnitRepository.save(testUnit);
        Long unitId = testUnit.getId();

        List<Long> ids = new ArrayList<>();
        ids.add(unitId);

        AddFacilitiesRequestDTO dto = new AddFacilitiesRequestDTO(ids);

        assertThrows(EntityNotFoundException.class, () -> bookableUnitService.addFacilitiesToUnit(unitId, dto));
    }

    @Test
    @DisplayName("Should successfully search for units based on criteria")
    void searchUnits() {

        Property property = new Property();
        property.setCity("Novi Sad");
        property.setCountry("Serbia");
        property = propertyRepository.save(property);


        BookableUnit unit = new BookableUnit();
        unit.setProperty(property);
        unit.setMaxCapacity(4);
        unit.setMaxAdultCapacity(2);
        unit.setMaxKidsCapacity(2);
        unit.setTotalUnits(5);
        unit = bookableUnitRepository.save(unit);


        PeriodPrice price = new PeriodPrice();
        price.setBookableUnit(unit);
        price.setStartDate(LocalDate.of(2026, 6, 1));
        price.setEndDate(LocalDate.of(2026, 6, 30));
        price.setPricePerNight(100.0);
        periodPriceRepository.save(price);

        List<PeriodPrice> priceList = new ArrayList<>();
        priceList.add(price);
        unit.setPeriodPriceList(priceList);

        bookableUnitRepository.save(unit);


        List<BookableUnitCardDTO> results = bookableUnitService.searchUnits(
                "Novi Sad", "Serbia", 2, 0,
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 15),
                null, null, null
        );

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(unit.getId(), results.getFirst().unitId());
        assertEquals(500.0, results.getFirst().totalPriceForStay());
    }

    @Test
    @DisplayName("Should filter units by both Property and Unit facilities")
    void searchUnits_WithFacilityFilters() {

        Fascillity wifi = fasiliityRepository.save(new Fascillity("WiFi"));
        Fascillity pool = fasiliityRepository.save(new Fascillity("Pool"));


        UnitFascillity ac = unitFascilityRepository.save(new UnitFascillity("AC"));
        UnitFascillity tv = unitFascilityRepository.save(new UnitFascillity("TV"));


        Property matchingProperty = new Property();
        matchingProperty.setCity("Belgrade");
        matchingProperty.setCountry("Serbia");
        matchingProperty = propertyRepository.save(matchingProperty);


        propertyFascilityRepository.save(new PropertyFacility(matchingProperty, wifi));
        propertyFascilityRepository.save(new PropertyFacility(matchingProperty, pool));

        BookableUnit matchingUnit = new BookableUnit();
        matchingUnit.setProperty(matchingProperty);
        matchingUnit.setMaxCapacity(5);
        matchingUnit.setMaxAdultCapacity(3);
        matchingUnit.setMaxKidsCapacity(2);
        matchingUnit.setTotalUnits(1);
        matchingUnit = bookableUnitRepository.save(matchingUnit);


        unitFascillityMappingRepository.save(new UnitFascilityMapping(matchingUnit, ac));
        unitFascillityMappingRepository.save(new UnitFascilityMapping(matchingUnit, tv));

        // Add Pricing for the matching unit
        PeriodPrice price = new PeriodPrice();
        price.setBookableUnit(matchingUnit);
        price.setStartDate(LocalDate.of(2026, 7, 1));
        price.setEndDate(LocalDate.of(2026, 7, 31));
        price.setPricePerNight(100.0);
        periodPriceRepository.save(price);
        matchingUnit.setPeriodPriceList(new ArrayList<>(List.of(price)));


        Property partialProperty = new Property();
        partialProperty.setCity("Belgrade");
        partialProperty.setCountry("Serbia");
        partialProperty = propertyRepository.save(partialProperty);
        propertyFascilityRepository.save(new PropertyFacility(partialProperty, wifi));


        BookableUnit nonMatchingUnit = new BookableUnit();
        nonMatchingUnit.setProperty(partialProperty);
        nonMatchingUnit.setMaxCapacity(5);
        nonMatchingUnit.setMaxAdultCapacity(3);
        nonMatchingUnit.setMaxKidsCapacity(2);
        nonMatchingUnit.setTotalUnits(1);
        bookableUnitRepository.save(nonMatchingUnit);


        List<Long> propFacIds = List.of(wifi.getId(), pool.getId());
        List<Long> unitFacIds = List.of(ac.getId(), tv.getId());

        List<BookableUnitCardDTO> results = bookableUnitService.searchUnits(
                "Belgrade", "Serbia", 2, 0,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 12),
                null, propFacIds, unitFacIds
        );


        assertNotNull(results);
        assertEquals(1, results.size(), "Should only find the unit that has ALL requested facilities");
        assertEquals(matchingUnit.getId(), results.get(0).unitId());
    }
}
