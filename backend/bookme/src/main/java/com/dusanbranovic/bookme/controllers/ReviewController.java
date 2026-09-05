package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/bookings/{bookingId}/reviews")
    public ResponseEntity<ReviewResponseDTO> addReview(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ReviewRequestDTO dto,
            Authentication authentication
    ) {

        ReviewResponseDTO review =
                reviewService.addReview(
                        bookingId,
                        dto,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(review);
    }


    @GetMapping("/units/{unitId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> getUnitReviews(
            @PathVariable UUID unitId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                reviewService.getUnitReviews(
                        unitId,
                        pageable
                )
        );
    }


    @GetMapping("/properties/{propertyId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> getPropertyReviews(
            @PathVariable UUID propertyId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                reviewService.getPropertyReviews(
                        propertyId,
                        pageable
                )
        );
    }

}
