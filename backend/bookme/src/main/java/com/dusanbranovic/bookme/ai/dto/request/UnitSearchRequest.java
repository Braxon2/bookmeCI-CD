package com.dusanbranovic.bookme.ai.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitSearchRequest(@NotBlank @Size(max = 2000) String message, @Valid UnitSearchContext context) {
}
