package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonRequestDTO;
import com.dusanbranovic.bookme.dto.requests.AddonToAddRequestDTO;
import com.dusanbranovic.bookme.dto.requests.BillingTypeRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceAddonRequestDTO;
import com.dusanbranovic.bookme.dto.responses.*;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.InvalidPriceValueException;
import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceAddonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AddonService {

    private final AddonRepository addonRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final PeriodPriceAddonRepository periodPriceAddonRepository;
    private final AddonMappingRepository addonMappingRepository;

    private static final Logger log = LoggerFactory.getLogger(AddonService.class);

    public AddonService(
            AddonRepository addonRepository,
            BookableUnitRepository bookableUnitRepository,
            PeriodPriceAddonRepository periodPriceAddonRepository,
            AddonMappingRepository addonMappingRepository
    ) {
        this.addonRepository = addonRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.periodPriceAddonRepository = periodPriceAddonRepository;
        this.addonMappingRepository = addonMappingRepository;
    }

    public AddonToAddResponseDTO addAddonToUnit(UUID unitId, AddonToAddRequestDTO dto) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() -> {
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        Addon addon = addonRepository.findById(dto.id()).orElseThrow(() -> {
            log.error("Addon not found");
            return new EntityNotFoundException("Addon with id " + dto.id() + " not found");
        });

        Optional<AddonMapping> existingMapping = addonMappingRepository.findByAddonAndUnitID(unitId, dto.id());
        if (existingMapping.isPresent()) {
            log.error("This addon is already mapped to this unit.");
            throw new EntityAlreadyExistsExcpetion("This addon is already mapped to this unit.");
        }

        AddonMapping addonMapping = new AddonMapping(false,unit,addon);
        AddonMapping savedAddonMapping = addonMappingRepository.save(addonMapping);
        log.info("Addon successfully added to unit");

        return new AddonToAddResponseDTO(savedAddonMapping.getId(),savedAddonMapping.getAddon().getName(), savedAddonMapping.isPerNight());
    }

    public List<AddonResponseDTO> getAllAddons() {

        return addonRepository.findAll().
                stream().map(
                        addon ->
                                new AddonResponseDTO(
                                        addon.getId()
                                        ,addon.getName()
                                )
                ).collect(Collectors.toList());
    }


    public AddonPeriodPriceResponseDTO addAddonPeriodPrice(
            UUID unitId,
            Long addonId,
            PeriodPriceAddonRequestDTO dto
    ) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() -> {
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        Addon addon = addonRepository.findById(addonId).orElseThrow(() -> {
            log.error("Addon not found");
            return new EntityNotFoundException("Addon with id " + addonId + " not found");
        });

        AddonMapping addonMapping = addonMappingRepository.findByAddonAndUnitID(unitId,addonId).orElseThrow(() ->{
            log.error("Addon not found in unit");
            return new EntityNotFoundException("Addon with id " + addonId + " not found in unit with id " + unitId);
        });

        LocalDate start = dto.startDate();
        LocalDate end = dto.endDate();

        if (!start.isBefore(end)) {
            log.error("Start date must be before end date");
            throw new InvalidDateRangeException("Start date must be before end date");
        }

        if(dto.price() <= 0){
            log.error("Price must be greater than 0");
            throw new InvalidPriceValueException("Price must be greater than 0");
        }

        PeriodPriceAddon periodPriceAddon = new PeriodPriceAddon(addonMapping,dto.price(),dto.startDate(),dto.endDate());

        addonMapping.getPeriodPriceAddons().add(periodPriceAddon);

        periodPriceAddonRepository.save(periodPriceAddon);


        return new AddonPeriodPriceResponseDTO(
                periodPriceAddon.getId(),
                periodPriceAddon.getPrice(),
                periodPriceAddon.getStartDate(),
                periodPriceAddon.getEndDate(),
                new AddonResponseDTO(addon.getId(), addon.getName())
        );

    }

    public AddonResponseDTO addAddon(AddonRequestDTO dto) {
        Optional<Addon> optionalAddon = addonRepository.findByName(dto.name());

        if(optionalAddon.isPresent()) {
            throw new EntityAlreadyExistsExcpetion("Addon with name " + dto.name() + " already exist");
        }

        Addon addon = new Addon();
        addon.setName(dto.name());

        log.info("REQUEST: name of addon " + dto.name());

        Addon savedAddon = addonRepository.save(addon);
        log.info("Body of saved addon {} " + savedAddon.getName());
        return new AddonResponseDTO(savedAddon.getId(), savedAddon.getName());


    }

    public boolean changeBillingType(UUID unitId, Long addonId, BillingTypeRequestDTO dto) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() -> {
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        Addon addon = addonRepository.findById(addonId).orElseThrow(() -> {
            log.error("Addon not found");
            return new EntityNotFoundException("Addon with id " + addonId + " not found");
        });

        AddonMapping addonMapping = addonMappingRepository.findByAddonAndUnitID(unitId,addonId).orElseThrow(() ->{
            log.error("Addon not found in unit");
            return new EntityNotFoundException("Addon with id " + addonId + " not found in unit with id " + unitId);
        });
        addonMapping.setPerNight(dto.isPerNight());
        addonMappingRepository.save(addonMapping);

        return addonMapping.isPerNight();
    }

    public List<PeriodPriceAddonResponseDTO> getPeriodPrices(UUID unitId, Long addonId) {

        BookableUnit unit = bookableUnitRepository.findByPublicId(unitId).orElseThrow(() -> {
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });

        Addon addon = addonRepository.findById(addonId).orElseThrow(() -> {
            log.error("Addon not found");
            return new EntityNotFoundException("Addon with id " + addonId + " not found");
        });

        AddonMapping addonMapping = addonMappingRepository.findByAddonAndUnitID(unitId,addonId).orElseThrow(() ->{
            log.error("Addon not found in unit");
            return new EntityNotFoundException("Addon with id " + addonId + " not found in unit with id " + unitId);
        });

        return addonMapping
                .getPeriodPriceAddons()
                .stream()
                .map(periodPrice ->
                        new PeriodPriceAddonResponseDTO(
                                periodPrice.getId(),
                                periodPrice.getPrice(),
                                periodPrice.getAddonMapping().isPerNight(),
                                periodPrice.getStartDate(),
                                periodPrice.getEndDate())
                )
                .collect(Collectors.toList());
    }
}
