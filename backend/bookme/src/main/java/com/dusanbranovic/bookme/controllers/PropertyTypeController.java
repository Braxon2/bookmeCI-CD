package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.dto.requests.PropertyTypeRequestDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyTypeDTO;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.service.PropertyTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property-type")
public class PropertyTypeController {
    private final PropertyTypeService propertyTypeService;

    public PropertyTypeController(PropertyTypeService propertyTypeService) {
        this.propertyTypeService = propertyTypeService;
    }

    @GetMapping
    public List<PropertyType> getAll(){
        return propertyTypeService.getAll();
    }

    @PostMapping
    public PropertyType addType(@RequestBody PropertyTypeRequestDTO dto){
        return propertyTypeService.addType(dto);
    }
}
