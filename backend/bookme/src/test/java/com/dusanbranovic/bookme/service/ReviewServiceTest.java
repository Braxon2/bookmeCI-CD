package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.ReviewAuthorDTO;
import com.dusanbranovic.bookme.dto.requests.ReviewRequestDTO;
import com.dusanbranovic.bookme.dto.responses.ReviewResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.ReviewNotAllowedException;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.Review;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID bookingId;
    private UUID unitId;
    private UUID propertyId;
    private User guest;
    private Booking booking;
    private Review review;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        guest = new User();
        guest.setFirstName("Ana");
        guest.setLastName("Guest");
        BookableUnit unit = new BookableUnit();
        unit.setPublicId(unitId);
        unit.setName("Garden room");
        Property property = new Property();
        unit.setProperty(property);
        booking = new Booking();
        booking.setId(12L);
        booking.setPublicId(bookingId);
        booking.setBookableUnit(unit);
        booking.setGuest(guest);
        booking.setStatus(BookingStatus.COMPLETED);
        review = new Review(booking, 5, "Excellent stay", LocalDateTime.now());
        review.setId(22L);
    }

    @Test
    void addReviewCreatesAReviewForACompletedBooking() {
        when(bookingRepository.findByPublicIdAndGuest_Email(bookingId, "guest@test.com"))
                .thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking_Id(12L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toReviewAuthorDTO(guest)).thenReturn(new ReviewAuthorDTO("Ana", "Guest"));

        ReviewResponseDTO result = reviewService.addReview(
                bookingId,
                new ReviewRequestDTO(5, "Excellent stay"),
                "guest@test.com"
        );

        assertEquals(5, result.rating());
        assertEquals("Excellent stay", result.text());
        assertEquals(unitId, result.bookableUnitPublicId());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReviewRejectsAnUnknownBooking() {
        when(bookingRepository.findByPublicIdAndGuest_Email(bookingId, "guest@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.addReview(
                        bookingId, new ReviewRequestDTO(4, "Good stay"), "guest@test.com"
                )
        );
    }

    @Test
    void addReviewRejectsABookingThatIsNotCompleted() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByPublicIdAndGuest_Email(bookingId, "guest@test.com"))
                .thenReturn(Optional.of(booking));

        assertThrows(
                ReviewNotAllowedException.class,
                () -> reviewService.addReview(
                        bookingId, new ReviewRequestDTO(4, "Good stay"), "guest@test.com"
                )
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReviewRejectsASecondReviewForTheSameBooking() {
        when(bookingRepository.findByPublicIdAndGuest_Email(bookingId, "guest@test.com"))
                .thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking_Id(12L)).thenReturn(true);

        assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> reviewService.addReview(
                        bookingId, new ReviewRequestDTO(4, "Good stay"), "guest@test.com"
                )
        );
    }

    @Test
    void getUnitReviewsMapsAReviewPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookableUnitRepository.existsByPublicId(unitId)).thenReturn(true);
        when(reviewRepository.findByBooking_BookableUnit_PublicId(unitId, pageable))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));
        when(userMapper.toReviewAuthorDTO(guest)).thenReturn(new ReviewAuthorDTO("Ana", "Guest"));

        Page<ReviewResponseDTO> result = reviewService.getUnitReviews(unitId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Garden room", result.getContent().getFirst().bookableUnitName());
    }

    @Test
    void getUnitReviewsRejectsAnUnknownUnit() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookableUnitRepository.existsByPublicId(unitId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> reviewService.getUnitReviews(unitId, pageable));
        verify(reviewRepository, never()).findByBooking_BookableUnit_PublicId(any(), any());
    }

    @Test
    void getPropertyReviewsMapsAReviewPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(propertyRepository.existsByPublicId(propertyId)).thenReturn(true);
        when(reviewRepository.findByBooking_BookableUnit_Property_PublicId(propertyId, pageable))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));
        when(userMapper.toReviewAuthorDTO(guest)).thenReturn(new ReviewAuthorDTO("Ana", "Guest"));

        Page<ReviewResponseDTO> result = reviewService.getPropertyReviews(propertyId, pageable);

        assertFalse(result.isEmpty());
        assertEquals(5, result.getContent().getFirst().rating());
    }

    @Test
    void getPropertyReviewsRejectsAnUnknownProperty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(propertyRepository.existsByPublicId(propertyId)).thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.getPropertyReviews(propertyId, pageable)
        );
    }
}
