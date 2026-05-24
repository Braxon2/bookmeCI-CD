package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.models.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final BookableUnitMapper bookableUnitMapper;
    private final UserMapper userMapper;

    public BookingMapper(BookableUnitMapper bookableUnitMapper, UserMapper userMapper) {
        this.bookableUnitMapper = bookableUnitMapper;
        this.userMapper = userMapper;
    }

    public BookingResponseDTO toDTO(Booking booking){
        return new BookingResponseDTO(
                booking.getId(),
                bookableUnitMapper.toDTO(booking.getBookableUnit()),
                userMapper.toDTO(booking.getGuest()),
                booking.getTotalPrice(),
                booking.getCreatedAt(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getStatus()
        );
    }

    public Booking toEntity(BookingResponseDTO dto){
        Booking booking = new Booking();
        booking.setBookableUnit(bookableUnitMapper.toEntity(dto.bookableUnit()));
        booking.setGuest(userMapper.toEntity(dto.guest()));
        booking.setCheckIn(dto.checkIn());
        booking.setCheckOut(dto.checkOut());
        booking.setTotalPrice(dto.totalPrice());
        booking.setStatus(dto.status());
        booking.setCreatedAt(dto.createdAt());


        return booking;
    }
}
