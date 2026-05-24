package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonsRequestDTO;
import com.dusanbranovic.bookme.dto.requests.BookingRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PeriodPriceRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitCardDTO;
import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.dto.responses.PeriodPriceResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.PeriodPriceMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BookableUnitServiceTest {


    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private PeriodPriceRepository periodPriceRepository;
    @Mock
    private BookableUnitMapper bookableUnitMapper;
    @Mock
    private PeriodPriceMapper periodPriceMapper;

    @InjectMocks
    private BookableUnitService bookableUnitService;


    @Test
    void addPeriodPrice_unitNotFound_throwsException() {
        when(bookableUnitRepository.findById(1L))
                .thenReturn(Optional.empty());

        PeriodPriceRequestDTO dto =
                new PeriodPriceRequestDTO(100, LocalDate.now(),
                        LocalDate.now().plusDays(5), "SUMMER");

        assertThrows(EntityNotFoundException.class,
                () -> bookableUnitService.addPeriodPrice(1L, dto));
    }

    @Test
    void addPeriodPrice_validData_savesPrice() {
        BookableUnit unit = new BookableUnit();
        PeriodPrice price = new PeriodPrice();
        PeriodPriceResponseDTO responseDTO = mock(PeriodPriceResponseDTO.class);

        when(bookableUnitRepository.findById(1L))
                .thenReturn(Optional.of(unit));
        when(periodPriceMapper.toEntity(any(), eq(unit)))
                .thenReturn(price);
        when(periodPriceRepository.save(price))
                .thenReturn(price);
        when(periodPriceMapper.toDTO(price))
                .thenReturn(responseDTO);

        PeriodPriceResponseDTO result =
                bookableUnitService.addPeriodPrice(1L,
                        new PeriodPriceRequestDTO(100,
                                LocalDate.now(),
                                LocalDate.now().plusDays(3),
                                "SUMMER"));

        assertNotNull(result);
    }

    @Test
    void getPeriodPrices_returnsMappedList() {
        BookableUnit unit = new BookableUnit();
        PeriodPrice price = new PeriodPrice();
        unit.setPeriodPriceList(List.of(price));

        when(bookableUnitRepository.findById(1L))
                .thenReturn(Optional.of(unit));
        when(periodPriceMapper.toDTO(price))
                .thenReturn(mock(PeriodPriceResponseDTO.class));

        List<PeriodPriceResponseDTO> result =
                bookableUnitService.getPeriodPrices(1L);

        assertEquals(1, result.size());
    }






}