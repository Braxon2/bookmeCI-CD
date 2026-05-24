package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.dto.requests.FascilityRequestDTO;
import com.dusanbranovic.bookme.dto.responses.FascilityResponseDTO;
import com.dusanbranovic.bookme.service.FascilityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fascilities")
public class FascilityController {

    private final FascilityService fascilityService;

    public FascilityController(FascilityService fascilityService) {
        this.fascilityService = fascilityService;
    }

    @PostMapping
    public FascilityResponseDTO addFascility(@RequestBody FascilityRequestDTO dto){
        return fascilityService.addFascility(dto);
    }

    @GetMapping
    public List<FascilityResponseDTO> getFascilities(){
        return fascilityService.getFascilities();
    }
}
