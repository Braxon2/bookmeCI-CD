package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.ReviewNotAllowedException;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.models.Review;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    public ReviewService(ReviewRepository reviewRepository,
                         BookableUnitRepository bookableUnitRepository,
                         PropertyRepository propertyRepository,
                         BookingRepository bookingRepository,
                         UserMapper userMapper
    ) {
        this.reviewRepository = reviewRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public ReviewResponseDTO addReview(
            UUID bookingPublicId,
            ReviewRequestDTO dto,
            String email
    ) {

        Booking booking = bookingRepository
                .findByPublicIdAndGuest_Email(
                        bookingPublicId,
                        email
                )
                .orElseThrow(() -> {
                    log.error("Booking not found");
                    return new EntityNotFoundException(
                            "Booking with id "
                                    + bookingPublicId
                                    + " not found"
                    );
                });



        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewNotAllowedException(
                    "Only completed bookings can be reviewed"
            );
        }


        if (reviewRepository.existsByBooking_Id(booking.getId())) {
            throw new EntityAlreadyExistsExcpetion(
                    "This booking has already been reviewed"
            );
        }


        Review review = new Review();

        review.setBooking(booking);
        review.setRating(dto.rating());
        review.setText(dto.text());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        log.info(
                "Review successfully created for booking {}",
                bookingPublicId
        );

        return toResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getUnitReviews(
            UUID unitPublicId,
            Pageable pageable
    ) {

        if (!bookableUnitRepository.existsByPublicId(unitPublicId)) {
            throw new EntityNotFoundException(
                    "Bookable unit with id "
                            + unitPublicId
                            + " not found"
            );
        }

        return reviewRepository
                .findByBooking_BookableUnit_PublicId(
                        unitPublicId,
                        pageable
                )
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getPropertyReviews(
            UUID propertyPublicId,
            Pageable pageable
    ) {

        if (!propertyRepository.existsByPublicId(propertyPublicId)) {
            throw new EntityNotFoundException(
                    "Property with id "
                            + propertyPublicId
                            + " not found"
            );
        }

        return reviewRepository
                .findByBooking_BookableUnit_Property_PublicId(
                        propertyPublicId,
                        pageable
                )
                .map(this::toResponseDTO);
    }

    private ReviewResponseDTO toResponseDTO(Review review) {

        Booking booking = review.getBooking();
        BookableUnit unit = booking.getBookableUnit();

        return new ReviewResponseDTO(
                review.getPublicId(),
                review.getRating(),
                review.getText(),
                userMapper.toReviewAuthorDTO(booking.getGuest()),
                unit.getPublicId(),
                unit.getName(),
                review.getCreatedAt()
        );
    }
}
