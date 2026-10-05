package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.PropertyTypeRequestDTO;
import com.dusanbranovic.bookme.exceptions.EntityAlreadyExistsExcpetion;
import com.dusanbranovic.bookme.models.PropertyType;
import com.dusanbranovic.bookme.repository.PropertyTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyTypeServiceTest {

    @Mock
    private PropertyTypeRepository propertyTypeRepository;

    @InjectMocks
    private PropertyTypeService propertyTypeService;

    @Test
    void getAllReturnsRepositoryContents() {
        List<PropertyType> types = List.of(
                new PropertyType(1L, "Hotel"),
                new PropertyType(2L, "Apartment")
        );
        when(propertyTypeRepository.findAll()).thenReturn(types);

        assertSame(types, propertyTypeService.getAll());
    }

    @Test
    void addTypeSavesAUniquePropertyType() {
        PropertyTypeRequestDTO request = new PropertyTypeRequestDTO("Villa");
        PropertyType saved = new PropertyType(9L, "Villa");
        when(propertyTypeRepository.findByName("Villa")).thenReturn(Optional.empty());
        when(propertyTypeRepository.save(any(PropertyType.class))).thenReturn(saved);

        PropertyType result = propertyTypeService.addType(request);

        assertSame(saved, result);
        verify(propertyTypeRepository).save(any(PropertyType.class));
    }

    @Test
    void addTypeRejectsADuplicateName() {
        PropertyTypeRequestDTO request = new PropertyTypeRequestDTO("Hotel");
        when(propertyTypeRepository.findByName("Hotel"))
                .thenReturn(Optional.of(new PropertyType(1L, "Hotel")));

        EntityAlreadyExistsExcpetion exception = assertThrows(
                EntityAlreadyExistsExcpetion.class,
                () -> propertyTypeService.addType(request)
        );

        assertEquals("Property type already exist", exception.getMessage());
        verify(propertyTypeRepository, never()).save(any());
    }
}
