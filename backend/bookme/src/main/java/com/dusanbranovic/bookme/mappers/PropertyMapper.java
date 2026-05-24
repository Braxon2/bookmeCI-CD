package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.models.Property;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;

@Component
public class PropertyMapper {

    private final UserMapper userMapper;
    private final PropertyTypeMapper propertyTypeMapper;
    private final FascilityMapper fascilityMapper;

    public PropertyMapper(
            UserMapper userMapper,
            PropertyTypeMapper propertyTypeMapper, FascilityMapper fascilityMapper
    ) {
        this.userMapper = userMapper;
        this.propertyTypeMapper = propertyTypeMapper;
        this.fascilityMapper = fascilityMapper;
    }

    public PropertyDTO toDTO(Property property){
        var facilities = property.getPropertyFacilities() == null
                ? List.<FascilityResponseDTO>of()
                : property.getPropertyFacilities().stream()
                .map(fascilityMapper::toDTO)
                .collect(Collectors.toList());
        return new PropertyDTO(property.getId(),
                propertyTypeMapper.toDTO(property.getPropertyType()),
                property.getName(),
                property.getDescription(),
                property.getCountry(),
                property.getCity(),
                property.getAddress(),
                property.getHouseRules(),
                property.getImportantInfo(),
                facilities
        );
    }

//    public Property toProperty(PropertyDTO dto){
//        return new Property(dto.id(),
//                userMapper.toUser(dto.ownerDTO()),
//                propertyTypeMapper.toPropertyType(dto.propertyTypeDTO()),
//                dto.name(),
//                dto.description(),
//                dto.country(),
//                dto.city(),
//                dto.address(),
//                dto.houseRules(),
//                dto.importantInfo());
//    }
}
