package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.dto.requests.AddonRequestDTO;
import com.dusanbranovic.bookme.dto.responses.AddonResponseDTO;
import com.dusanbranovic.bookme.service.AddonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
public class AddonController {

    private final AddonService addonService;

    public AddonController(AddonService addonService) {
        this.addonService = addonService;
    }

    @GetMapping
    public List<AddonResponseDTO> getAllAddons(){
        return addonService.getAllAddons();
    }

    @PostMapping
    public AddonResponseDTO addAddon(@RequestBody AddonRequestDTO dto){
        return addonService.addAddon(dto);
    }

}
