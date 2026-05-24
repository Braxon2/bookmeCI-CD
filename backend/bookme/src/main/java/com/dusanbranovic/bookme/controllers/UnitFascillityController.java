package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.requests.UnitFascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.UnitFascilityResponseDTO;
import com.dusanbranovic.bookme.service.UnitFascillityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit-fascilities")
public class UnitFascillityController {

    private final UnitFascillityService unitFascillityService;

    public UnitFascillityController(UnitFascillityService unitFascillityService) {
        this.unitFascillityService = unitFascillityService;
    }

    @PostMapping
    public UnitFascilityResponseDTO addFascility(@RequestBody UnitFascilityRequestDTO dto){
        return unitFascillityService.addUnitFascility(dto);
    }

    @GetMapping
    public List<UnitFascilityResponseDTO> getUnitFasilities(){
        return unitFascillityService.getUnitFasilities();
    }
}
