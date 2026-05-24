package com.dusanbranovic.bookme.integrations.service;

import com.dusanbranovic.bookme.dto.requests.BookingRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.OverlappingBookingExcpetion;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import com.dusanbranovic.bookme.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceIT {

    @Autowired
    private  BookingRepository bookingRepository;

    @Autowired
    private  BookableUnitRepository bookableUnitRepository;

    @Autowired
    private  AddonRepository addonRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyRepository propertyRepository;


    @Autowired
    private PeriodPriceRepository periodPriceRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){

    }

    @Test
    @DisplayName("Should sucessfully book a Unit")
    void bookAUnit(){
        PropertyType type = new PropertyType("Beach House");
        propertyTypeRepository.save(type);

        Property property = new Property();
        property.setName("Santa fe");
        property.setPropertyType(type);

        BookableUnit unit = new BookableUnit();
        property.addUnit(unit);
        unit.setTotalUnits(10);
        unit.setName("Charlies House");

        LocalDate start = LocalDate.of(2026,1,10);
        LocalDate  endDate = LocalDate.of(2026,3,8);

        PeriodPrice periodPrice = new PeriodPrice();
        periodPrice.setPricePerNight(240.50);
        periodPrice.setStartDate(start);
        periodPrice.setEndDate(endDate);
        periodPrice.setSeason("Zeesty");
        unit.addPeriodPrice(periodPrice);

        propertyRepository.save(property);
        bookableUnitRepository.save(unit);
        periodPriceRepository.save(periodPrice);

        User guest = new User();
        guest.setEmail("guest@test.com");
        userRepository.save(guest);

        Authentication auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        BookingRequestDTO dto = new BookingRequestDTO(start,endDate,new ArrayList<>());

        BookingResponseDTO result = bookingService.bookAUnit(unit.getId(), dto);

        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED,result.status());
    }

    @Test
    @DisplayName("Should throw InvalidDateRangeException")
    void bookAUnit_InvalidDateRange(){
        PropertyType type = new PropertyType("Beach House");
        propertyTypeRepository.save(type);

        Property property = new Property();
        property.setName("Santa fe");
        property.setPropertyType(type);

        BookableUnit unit = new BookableUnit();
        property.addUnit(unit);
        unit.setName("Charlies House");

        LocalDate start = LocalDate.of(2027,1,10);
        LocalDate  endDate = LocalDate.of(2026,3,8);

        PeriodPrice periodPrice = new PeriodPrice();
        periodPrice.setPricePerNight(240.50);
        periodPrice.setStartDate(start);
        periodPrice.setEndDate(endDate);
        periodPrice.setSeason("Zeesty");
        unit.addPeriodPrice(periodPrice);

        propertyRepository.save(property);

        bookableUnitRepository.save(unit);

        User guest = new User();
        guest.setEmail("guest@test.com");
        userRepository.save(guest);

        Authentication auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        BookingRequestDTO dto = new BookingRequestDTO(start,endDate,new ArrayList<>());

        assertThrows(InvalidDateRangeException.class, () -> bookingService.bookAUnit(unit.getId(), dto));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException")
    void bookAUnit_UnitNotFound(){
        LocalDate start = LocalDate.of(2026,1,10);
        LocalDate  endDate = LocalDate.of(2026,3,8);

        BookingRequestDTO dto = new BookingRequestDTO(start,endDate,new ArrayList<>());

        assertThrows(EntityNotFoundException.class, () -> bookingService.bookAUnit(0L, dto));
    }

    @Test
    @DisplayName("Should throw OverLappingBookingException")
    void bookAUnit_OverLappingBookings(){
        PropertyType type = new PropertyType("Beach House");
        propertyTypeRepository.save(type);

        Property property = new Property();
        property.setName("Santa fe");
        property.setPropertyType(type);

        BookableUnit unit = new BookableUnit();
        property.addUnit(unit);
        unit.setName("Charlies House");

        LocalDate start = LocalDate.of(2026,1,10);
        LocalDate  endDate = LocalDate.of(2026,3,8);

        PeriodPrice periodPrice = new PeriodPrice();
        periodPrice.setPricePerNight(240.50);
        periodPrice.setStartDate(start);
        periodPrice.setEndDate(endDate);
        periodPrice.setSeason("Zeesty");
        unit.addPeriodPrice(periodPrice);


        propertyRepository.save(property);

        bookableUnitRepository.save(unit);

        User guest = new User();
        guest.setEmail("guest@test.com");
        userRepository.save(guest);

        Booking booking = new Booking();
        booking.setBookableUnit(unit);
        booking.setCheckIn(start.atStartOfDay());
        booking.setCheckOut(endDate.atStartOfDay());

        bookingRepository.save(booking);

        Authentication auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        BookingRequestDTO dto = new BookingRequestDTO(start,endDate,new ArrayList<>());

        assertThrows(OverlappingBookingExcpetion.class, () -> bookingService.bookAUnit(unit.getId(), dto));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException")
    void bookAUnit_PeriodPriceNotFound(){
        PropertyType type = new PropertyType("Beach House");
        propertyTypeRepository.save(type);

        Property property = new Property();
        property.setName("Santa fe");
        property.setPropertyType(type);

        BookableUnit unit = new BookableUnit();
        property.addUnit(unit);
        unit.setTotalUnits(10);
        unit.setName("Charlies House");

        LocalDate start = LocalDate.of(2026,1,10);
        LocalDate  endDate = LocalDate.of(2026,3,8);

        PeriodPrice periodPrice = new PeriodPrice();
        periodPrice.setPricePerNight(240.50);
        periodPrice.setStartDate(start);
        periodPrice.setEndDate(endDate);
        periodPrice.setSeason("Zeesty");
        unit.addPeriodPrice(periodPrice);

        propertyRepository.save(property);
        bookableUnitRepository.save(unit);
        periodPriceRepository.save(periodPrice);

        User guest = new User();
        guest.setEmail("guest@test.com");
        userRepository.save(guest);

        Authentication auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        LocalDate startBook = LocalDate.of(2026,3,10);
        LocalDate  endBook = LocalDate.of(2026,4,24);
        BookingRequestDTO dto = new BookingRequestDTO(startBook,endBook,new ArrayList<>());

        assertThrows(EntityNotFoundException.class, () -> bookingService.bookAUnit(unit.getId(), dto));


    }


}
