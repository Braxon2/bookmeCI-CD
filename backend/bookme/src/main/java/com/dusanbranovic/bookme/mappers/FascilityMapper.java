package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.models.PropertyFacility;
import org.springframework.stereotype.Component;

@Component
public class FascilityMapper {

    public FascilityResponseDTO toDTO(PropertyFacility propertyFacility){
        return new FascilityResponseDTO(propertyFacility.getFacility().getId(),propertyFacility.getFacility().getName());
    }
}
