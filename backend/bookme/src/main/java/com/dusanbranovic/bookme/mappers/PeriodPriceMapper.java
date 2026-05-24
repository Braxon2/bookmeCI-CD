package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.requests.PeriodPriceRequestDTO;
import com.dusanbranovic.bookme.dto.responses.PeriodPriceResponseDTO;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.PeriodPrice;
import org.springframework.stereotype.Component;

@Component
public class PeriodPriceMapper {

    private final BookableUnitMapper bookableUnitMapper;

    public PeriodPriceMapper(BookableUnitMapper bookableUnitMapper) {
        this.bookableUnitMapper = bookableUnitMapper;
    }

    public PeriodPrice toEntity(PeriodPriceRequestDTO dto, BookableUnit unit){
        PeriodPrice periodPrice = new PeriodPrice();
        periodPrice.setBookableUnit(unit);
        periodPrice.setPricePerNight(dto.pricePerNight());
        periodPrice.setSeason(dto.season());
        periodPrice.setStartDate(dto.startDate());
        periodPrice.setEndDate(dto.endDate());


        return periodPrice;
    }

    public PeriodPriceResponseDTO toDTO(PeriodPrice periodPrice) {
        return new PeriodPriceResponseDTO(
                periodPrice.getId(),
                bookableUnitMapper.toDTO(periodPrice.getBookableUnit()),
                periodPrice.getPricePerNight(),
                periodPrice.getStartDate(),
                periodPrice.getEndDate(),
                periodPrice.getSeason()
        );
    }
}
