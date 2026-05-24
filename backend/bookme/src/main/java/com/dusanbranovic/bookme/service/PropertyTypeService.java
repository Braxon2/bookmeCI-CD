package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.PropertyTypeRequestDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;

    public PropertyTypeService(PropertyTypeRepository propertyTypeRepository) {
        this.propertyTypeRepository = propertyTypeRepository;
    }


    public List<PropertyType> getAll() {
        return propertyTypeRepository.findAll();
    }

    public PropertyType addType(PropertyTypeRequestDTO dto) {
        Optional<PropertyType> optionalPropertyType = propertyTypeRepository.findByName(dto.name());

        if(optionalPropertyType.isPresent()) throw new EntityAlreadyExistsExcpetion("Property type already exist");
        return propertyTypeRepository.save(new PropertyType(dto.name()));
    }
}
