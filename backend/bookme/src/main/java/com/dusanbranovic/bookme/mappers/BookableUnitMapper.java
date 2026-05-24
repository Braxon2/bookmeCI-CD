package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Property;
import org.springframework.stereotype.Component;

@Component
public class BookableUnitMapper {

    public BookableUnitsResponseDTO toDTO(BookableUnit unit){
        return new BookableUnitsResponseDTO(
                unit.getId(),
                unit.getMaxCapacity(),
                unit.getSquareMeters(),
                unit.getSingleBeds(),
                unit.getDoubleBeds(),
                unit.getMaxAdultCapacity(),
                unit.getMaxKidsCapacity(),
                unit.getName(),
                unit.getUnitFascilityMappings().stream().map(
                        ufas -> new UnitFascilityResponseDTO(
                                ufas.getUnitFascillity().getId(),
                                ufas.getUnitFascillity().getName()
                        )
                        ).toList()
        );
    }

    public BookableUnit toEntity(BookableUnitsResponseDTO dto){
        BookableUnit unit = new BookableUnit();
        unit.setId(dto.id());
        unit.setName(dto.name());
        unit.setMaxCapacity(dto.maxCapacity());
        unit.setMaxKidsCapacity(dto.maxKidsCapacity());
        unit.setMaxAdultCapacity(dto.maxAdultCapacity());
        unit.setDoubleBeds(dto.doubleBeds());
        unit.setSingleBeds(dto.singleBeds());
        unit.setSquareMeters(dto.squareMeters());

        return unit;

    }

    public BookableUnit toEntity(BookableUnitRequestDTO dto, Property property){
        BookableUnit unit = new BookableUnit();
        unit.setProperty(property);
        unit.setName(dto.name());
        unit.setMaxCapacity(dto.maxCapacity());
        unit.setMaxKidsCapacity(dto.maxKidsCapacity());
        unit.setMaxAdultCapacity(dto.maxAdultCapacity());
        unit.setDoubleBeds(dto.doubleBeds());
        unit.setSingleBeds(dto.singleBeds());
        unit.setSquareMeters(dto.squareMeters());


        return unit;

    }
}
