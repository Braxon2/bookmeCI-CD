package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.service.PropertyService;
import com.dusanbranovic.bookme.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);

    public PropertyController(PropertyService propertyService,
                              S3Service s3Service
    ) {
        this.propertyService = propertyService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public List<PropertyDTO> getAll(){
        return propertyService.getAll();
    }

    @PostMapping
    public PropertyDTO addPorperty(
            @RequestBody PropertyRequestDTO dto,
            Principal principal
    ){
        return propertyService.addProperty(dto, principal.getName());
    }

    @GetMapping("/{pid}")
    public PropertyDTO getProperty(@PathVariable Long pid){
        log.info("Fetching property with id: {} ", pid);
        return propertyService.getProperty(pid);
    }

    @GetMapping("/{pid}/units")
    public List<BookableUnitsResponseDTO> getAllUnits(@PathVariable Long pid){
        return propertyService.getAllUnits(pid);
    }

    @PostMapping("/{pid}/add-unit")
    public BookableUnitsResponseDTO addUnit(
            @PathVariable Long pid,
            @RequestBody BookableUnitRequestDTO dto
    ){
        return propertyService.addUnit(pid, dto);
    }

    @PostMapping("/{pid}/reviews")
    public ReviewResponseDTO addReview(
            @RequestBody ReviewRequestDTO dto,
            @PathVariable Long pid
    ){
        return propertyService.addReview(dto,pid);
    }

    @GetMapping("/{pid}/reviews")
    public List<ReviewResponseDTO> getReviews(
            @PathVariable Long pid
    ){
        return propertyService.getReviews(pid);
    }

    @PostMapping("/{pid}/images")
    public String uploadPropertyImage(
            @PathVariable Long pid,
            @RequestParam("image") MultipartFile file
    ){
        return s3Service.uploadPropertyImage(pid, file);
    }

    @GetMapping("/{pid}/images")
    public List<String> getPropertyImages(
            @PathVariable Long pid
    ){
        return propertyService.getPropertyImages(pid);
    }

    @GetMapping("/{pid}/thumbnail")
    public Map<String, String> getThumbnail(
            @PathVariable Long pid
    ){
        String url = propertyService.getThumbnail(pid);
        return Map.of("url", url);
    }


}
