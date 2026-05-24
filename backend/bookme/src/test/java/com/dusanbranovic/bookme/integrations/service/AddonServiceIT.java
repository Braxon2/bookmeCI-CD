package com.dusanbranovic.bookme.integrations.service;
import com.dusanbranovic.bookme.dto.requests.AddonRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceAddonRequestDTO;
import com.dusanbranovic.bookme.dto.responses.AddonPeriodPriceResponseDTO;
import com.dusanbranovic.bookme.dto.responses.AddonResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.InvalidPriceValueException;
import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import com.dusanbranovic.bookme.service.AddonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AddonServiceIT {

    @Autowired
    private AddonService addonService;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BookableUnitRepository bookableUnitRepository;

    @Autowired
    private AddonRepository addonRepository;

    private Addon addon;

    @BeforeEach
    public void setup(){
        addon = new Addon();
        addon.setName("WiFi");
        addonRepository.save(addon);
    }

//    @Test
//    @DisplayName("Should sucessfully save addon")
//    void addAddonToUnit(){
//        PropertyType type = new PropertyType("Beach House");
//        propertyTypeRepository.save(type);
//
//        Property property = new Property();
//        property.setName("Santa fe");
//        property.setPropertyType(type);
//
//        BookableUnit unit = new BookableUnit();
//        property.addUnit(unit);
//        unit.setTotalUnits(10);
//        unit.setName("Charlies House");
//
//        propertyRepository.save(property);
//        bookableUnitRepository.save(unit);
//
//        AddonRequestDTO dto = new AddonRequestDTO("Brakfast",true);
//
//        AddonResponseDTO result = addonService.addAddonToUnit(unit.getId(),dto);
//
//        assertNotNull(result);
//        assertEquals(dto.name(),result.name());
//    }
//
//    @Test
//    @DisplayName("Should trhow EntityAlreadyExistsExcpetion")
//    void addAddon_AddonToUnitAlreadyExist(){
//        PropertyType type = new PropertyType("Beach House");
//        propertyTypeRepository.save(type);
//
//        Property property = new Property();
//        property.setName("Santa fe");
//        property.setPropertyType(type);
//
//        BookableUnit unit = new BookableUnit();
//        property.addUnit(unit);
//        unit.setTotalUnits(10);
//        unit.setName("Charlies House");
//
//        propertyRepository.save(property);
//        bookableUnitRepository.save(unit);
//
//        AddonRequestDTO dto = new AddonRequestDTO("WiFi",true);
//
//        assertThrows(EntityAlreadyExistsExcpetion.class,() -> addonService.addAddonToUnit(unit.getId(),dto));
//    }
//
//    @Test
//    @DisplayName("Should thorw EntityNotFound for Unit")
//    void addAddon_ToUnit_UnitNotFound(){
//        PropertyType type = new PropertyType("Beach House");
//        propertyTypeRepository.save(type);
//
//        Property property = new Property();
//        property.setName("Santa fe");
//        property.setPropertyType(type);
//
//        BookableUnit unit = new BookableUnit();
//        property.addUnit(unit);
//        unit.setTotalUnits(10);
//        unit.setName("Charlies House");
//
//        propertyRepository.save(property);
//        bookableUnitRepository.save(unit);
//
//        AddonRequestDTO dto = new AddonRequestDTO("Brakfast",true);
//
//        assertThrows(EntityNotFoundException.class,() -> addonService.addAddonToUnit(0L,dto));
//    }

//    @Test
//    @DisplayName("Should sucessfully add period price for Addon")
//    void addPeriodPriceForAddon(){
//
//        Addon addon = new Addon();
//        addon.setName("Carl");
//        addonRepository.save(addon);
//
//        LocalDate start = LocalDate.of(2026,1,10);
//        LocalDate  end = LocalDate.of(2026,3,8);
//
//        PeriodPriceAddonRequestDTO dto = new PeriodPriceAddonRequestDTO(35.2,start,end);
//        AddonPeriodPriceResponseDTO result = addonService.addAddonPeriodPrice(addon.getId(),dto);
//
//        assertNotNull(result);
//        assertEquals(addon.getName(),result.addon().name());
//    }
//
//    @Test
//    @DisplayName("Should throw InvalidDateRangeEception")
//    void addPeriodPriceForAddon_InvalidDateRangeEception(){
//
//        Addon addon = new Addon();
//        addon.setName("Carl");
//        addonRepository.save(addon);
//
//        LocalDate start = LocalDate.of(2026,1,10);
//        LocalDate  end = LocalDate.of(2026,3,8);
//
//        PeriodPriceAddonRequestDTO dto = new PeriodPriceAddonRequestDTO(35.2,end,start);
//        assertThrows(InvalidDateRangeException.class, () ->addonService.addAddonPeriodPrice(addon.getId(), dto));
//
//    }
//
//    @Test
//    @DisplayName("Should throw ENtityNotFOundException for Unit")
//    void addPeriodPriceForAddon_UnitNotFound(){
//
//        Addon addon = new Addon();
//        addon.setName("Carl");
//        addonRepository.save(addon);
//
//        LocalDate start = LocalDate.of(2026,1,10);
//        LocalDate  end = LocalDate.of(2026,3,8);
//
//        PeriodPriceAddonRequestDTO dto = new PeriodPriceAddonRequestDTO(35.2,start,end);
//        assertThrows(EntityNotFoundException.class, () ->addonService.addAddonPeriodPrice(0L,dto));
//    }
//
//    @Test
//    @DisplayName("Should throw InvalidPriceValueException")
//    void addPeriodPriceForAddon_(){
//
//        Addon addon = new Addon();
//        addon.setName("Carl");
//        addonRepository.save(addon);
//
//        LocalDate start = LocalDate.of(2026,1,10);
//        LocalDate  end = LocalDate.of(2026,3,8);
//
//        PeriodPriceAddonRequestDTO dto = new PeriodPriceAddonRequestDTO(0,start,end);
//
//        assertThrows(InvalidPriceValueException.class, () ->addonService.addAddonPeriodPrice(addon.getId(),dto));
//    }


}
