package com.dusanbranovic.bookme.controllers;

import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.dto.responses.GuestSummaryDTO;

import com.dusanbranovic.bookme.service.PropertyService;
import com.dusanbranovic.bookme.service.UserService;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PropertyService propertyService;

    public UserController(
            UserService userService,
            PropertyService propertyService
    ) {
        this.userService = userService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<GuestSummaryDTO> getUsers(){
        return userService.getUsers();
    }

    @GetMapping("/{userID}")
    public GuestSummaryDTO getUserSummary(@PathVariable Long userID){
        return userService.getUserSummary(userID);
    }
    @GetMapping("/{userID}/properties")
    public List<PropertyDTO> getProperties(@PathVariable Long userID){
        return propertyService.getPropertiesFromOwner(userID);
    }

    @GetMapping("/{userID}/bookings")
    public List<BookingSummaryDTO> getBookings(@PathVariable Long userID){
        return userService.getBookings(userID);
    }

}
