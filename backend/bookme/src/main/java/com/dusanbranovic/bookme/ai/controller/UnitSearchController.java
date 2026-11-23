package com.dusanbranovic.bookme.ai.controller;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchRequest;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.service.UnitSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/ai/unit-search")
public class UnitSearchController {
    private final UnitSearchService service;
    public UnitSearchController(UnitSearchService service) { this.service = service; }

    @PostMapping
    public UnitSearchResponse interpret(@Valid @RequestBody UnitSearchRequest request, Principal principal) {
        return service.interpret(principal.getName(), request);
    }
}
