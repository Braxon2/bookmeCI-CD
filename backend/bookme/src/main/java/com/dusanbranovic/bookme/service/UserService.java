package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.dto.responses.GuestSummaryDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final BookableUnitMapper bookableUnitMapper;

    private final BookingRepository bookingRepository;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            BookableUnitMapper bookableUnitMapper,
            BookingRepository bookingRepository
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bookableUnitMapper = bookableUnitMapper;
        this.bookingRepository = bookingRepository;
    }

    public List<GuestSummaryDTO> getUsers() {
        return userRepository.findAll().
                stream().
                map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingSummaryDTO> getBookings(Long userID) {

        User guest = userRepository.findById(userID)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", userID);
                    return new EntityNotFoundException("User with ID " + userID + " not found");
                });

        return bookingRepository.findAllByGuestIdWithUnit(userID).stream().map(booking ->
                        new BookingSummaryDTO(
                                booking.getId(),
                                bookableUnitMapper.toDTO(booking.getBookableUnit()),
                                booking.getTotalPrice(),
                                booking.getCreatedAt(),
                                booking.getCheckIn(),
                                booking.getCheckOut(),
                                booking.getStatus())
        ).toList().reversed();

    }

    public GuestSummaryDTO getUserSummary(Long userID) {
        User guest = userRepository.findById(userID)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", userID);
                    return new EntityNotFoundException("User with ID " + userID + " not found");
                });

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if(!(guest.getId().equals(currentUser.getId()))){
            log.warn("User {} attempted to get user info  owned by user {}",
                    currentUser.getId(), userID);
            throw new AccessDeniedException("You do not have permission to cancel this booking.");
        }


        return new GuestSummaryDTO(
                guest.getId(),
                guest.getEmail(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getPhoneNumber()
        );
    }
}
