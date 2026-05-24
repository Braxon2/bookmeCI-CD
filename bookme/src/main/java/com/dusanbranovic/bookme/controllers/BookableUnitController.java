package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.requests.*;
import com.dusanbranovic.bookme.dto.responses.*;
import com.dusanbranovic.bookme.service.AddonService;
import com.dusanbranovic.bookme.service.BookableUnitService;
import com.dusanbranovic.bookme.service.BookingService;
import com.dusanbranovic.bookme.service.S3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/units")
public class BookableUnitController {

    private final BookableUnitService bookableUnitService;
    private final AddonService addonService;
    private final S3Service s3Service;

    private final BookingService bookingService;

    public BookableUnitController(
            BookableUnitService bookableUnitService,
            AddonService addonService,
            S3Service s3Service,
            BookingService bookingService
    ) {
        this.bookableUnitService = bookableUnitService;
        this.addonService = addonService;
        this.s3Service = s3Service;
        this.bookingService = bookingService;
    }

    @GetMapping("/{unit_id}")
    public BookableUnitSummaryDTO getUnit(
            @PathVariable Long unit_id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return bookableUnitService.getUnit(unit_id,startDate,endDate);
    }

    @GetMapping("/{unit_id}/info")
    public BookableUnitDetailedCardDTO getUnitInfo(
            @PathVariable Long unit_id
    ){
        return bookableUnitService.getUnitInfo(unit_id);
    }

    @PostMapping("/{unit_id}/add-price")
    public PeriodPriceResponseDTO addPeriodPrice(
            @PathVariable Long unit_id,
            @RequestBody PeriodPriceRequestDTO periodPriceDTO
    ){
        return bookableUnitService.addPeriodPrice(unit_id,periodPriceDTO);
    }

    @GetMapping("/{unit_id}/period-prices")
    public List<PeriodPriceResponseDTO> getPeriodPrices(
            @PathVariable Long unit_id
            ){
        return bookableUnitService.getPeriodPrices(unit_id);
    }


    @PostMapping("/{unit_id}/book")
    public BookingResponseDTO bookAUnit(
            @PathVariable Long unit_id,
            @RequestBody BookingRequestDTO bookingRequestDTO
    ){
        return bookingService.bookAUnit(unit_id,bookingRequestDTO);
    }

    @PostMapping("/{unitId}/addons")
    public AddonToAddResponseDTO addAddonToUnit(
            @PathVariable Long unitId,
            @RequestBody AddonToAddRequestDTO dto
    ){
        return addonService.addAddonToUnit(unitId,dto);
    }

    @PatchMapping("/{unitId}/addons/{addonId}/billing-type")
    public boolean changeBillingType(
            @PathVariable Long unitId,
            @PathVariable Long addonId,
            @RequestBody BillingTypeRequestDTO dto
    ){
        return addonService.changeBillingType(unitId,addonId,dto);
    }

    @PostMapping("/{unitId}/addons/{addonId}/add-price")
    public AddonPeriodPriceResponseDTO addAddonPeriodPrice(
            @RequestBody PeriodPriceAddonRequestDTO dto,
            @PathVariable Long unitId,
            @PathVariable Long addonId
    ){
        return addonService.addAddonPeriodPrice(unitId,addonId, dto);
    }

    @PostMapping("/{uid}/images")
    public String uploadUnitImage(
            @PathVariable Long uid,
            @RequestParam("image") MultipartFile file
    ){
        return s3Service.uploadUnitImage(uid, file);
    }


    @GetMapping("/search")
    public List<BookableUnitCardDTO> search(
            @RequestParam String city,
            @RequestParam String country,
            @RequestParam int adults,
            @RequestParam(defaultValue = "0") int kids,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<Long> propertyFacilities,
            @RequestParam(required = false) List<Long> unitFacilities
    ){
        return bookableUnitService.searchUnits(city, country, adults, kids, startDate, endDate,maxPrice, propertyFacilities, unitFacilities);
    }

    @PostMapping("/{unitId}/add-unit-facilities")
    public BookableUnitFacilitiesResponseDTO addFacilitiesToUnit(
            @PathVariable Long unitId,
            @RequestBody AddFacilitiesRequestDTO dto) {

        return bookableUnitService.addFacilitiesToUnit(unitId, dto);

    }


    @GetMapping("/{unit_id}/addons/{addon_id}/period-prices")
    public List<PeriodPriceAddonResponseDTO> getPeriodPrices(
            @PathVariable Long unit_id,
            @PathVariable Long addon_id
    ){
        return addonService.getPeriodPrices(unit_id,addon_id);
    }

    @GetMapping("/{unit_id}/addons")
    public List<BookableUnitAddonsResponseDTO> getUnitAddons(
            @PathVariable Long unit_id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return bookableUnitService.getUnitAddons(unit_id,startDate,endDate);
    }
}
