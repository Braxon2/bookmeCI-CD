package com.dusanbranovic.bookme.dto.requests;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        int rating,

        @NotBlank(message = "Review text cannot be empty")
        @Size(
                max = 3000,
                message = "Review cannot exceed 3000 characters"
        )
        String text
) {
}
