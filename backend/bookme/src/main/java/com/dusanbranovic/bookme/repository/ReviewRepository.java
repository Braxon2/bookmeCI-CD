package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    boolean existsByBooking_Id(Long bookingId);

    Page<Review> findByBooking_BookableUnit_PublicId(
            UUID unitPublicId,
            Pageable pageable
    );

    Page<Review> findByBooking_BookableUnit_Property_PublicId(
            UUID propertyPublicId,
            Pageable pageable
    );
}