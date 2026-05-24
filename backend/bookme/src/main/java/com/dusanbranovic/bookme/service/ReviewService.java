package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }
}
