package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.ImageResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.service.PropertyService;
import com.dusanbranovic.bookme.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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


    @GetMapping("/{propertyPublicId}")
    public PropertyDTO getProperty(
            @PathVariable UUID propertyPublicId
    ) {
        return propertyService.getProperty(propertyPublicId);
    }

    @GetMapping("/{pid}/units")
    public Page<BookableUnitsResponseDTO> getAllUnits(
            @PathVariable UUID pid,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return propertyService.getAllUnits(pid, pageable);
    }

    @PostMapping("/{pid}/add-unit")
    public BookableUnitsResponseDTO addUnit(
            @PathVariable UUID pid,
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

    @PostMapping("/{propertyPublicId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponseDTO uploadPropertyImage(
            @PathVariable UUID propertyPublicId,
            @RequestPart("image") MultipartFile file,
            Principal principal
    ) {
        return s3Service.uploadPropertyImage(
                propertyPublicId,
                file,
                principal.getName()
        );
    }

    @GetMapping("/{propertyPublicId}/images")
    public List<ImageResponseDTO> getPropertyImages(
            @PathVariable UUID propertyPublicId
    ) {
        return s3Service.getPropertyImages(propertyPublicId);
    }

    @GetMapping("/{propertyPublicId}/thumbnail")
    public Map<String, String> getThumbnail(
            @PathVariable UUID propertyPublicId
    ) {
        return Map.of(
                "url",
                s3Service.getPropertyThumbnail(propertyPublicId)
        );
    }


}
