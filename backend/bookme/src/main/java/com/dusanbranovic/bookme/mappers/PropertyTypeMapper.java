package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.models.PropertyType;
import org.springframework.stereotype.Component;

@Component
public class PropertyTypeMapper {

    public PropertyTypeDTO toDTO(PropertyType propertyType){
        return new PropertyTypeDTO(propertyType.getId(), propertyType.getName());
    }

    public PropertyType toPropertyType(PropertyTypeDTO dto){
        return new PropertyType(dto.id(), dto.name());
    }
}
