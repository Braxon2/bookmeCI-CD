package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.responses.ImageResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidFileTypeException;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyImage;
import com.dusanbranovic.bookme.models.UnitImage;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PropertyImageRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.UnitImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner s3Presigner;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private PropertyRepository propertyRepository;
    @Mock private BookableUnitRepository bookableUnitRepository;
    @Mock private UnitImageRepository unitImageRepository;
    @Mock private PropertyImageRepository propertyImageRepository;

    private S3Service s3Service;
    private UUID propertyId;
    private UUID unitId;

    @BeforeEach
    void setUp() {
        s3Service = spy(new S3Service(
                s3Client,
                s3Presigner,
                transactionTemplate,
                propertyRepository,
                bookableUnitRepository,
                unitImageRepository,
                propertyImageRepository
        ));
        propertyId = UUID.randomUUID();
        unitId = UUID.randomUUID();
    }

    @Test
    void getPropertyImagesMapsStoredMetadataAndPresignedUrls() {
        PropertyImage first = new PropertyImage("properties/one.jpg", true, 1, "image/jpeg", new Property());
        first.setId(1L);
        PropertyImage second = new PropertyImage("properties/two.png", false, 2, "image/png", new Property());
        second.setId(2L);
        when(propertyRepository.existsByPublicId(propertyId)).thenReturn(true);
        when(propertyImageRepository.findAllByProperty_PublicIdOrderBySortOrderAsc(propertyId))
                .thenReturn(List.of(first, second));
        doReturn("https://images.test/one").when(s3Service).createPresignedGetUrl("properties/one.jpg");
        doReturn("https://images.test/two").when(s3Service).createPresignedGetUrl("properties/two.png");

        List<ImageResponseDTO> result = s3Service.getPropertyImages(propertyId);

        assertEquals(2, result.size());
        assertEquals("https://images.test/one", result.getFirst().url());
        assertEquals(1, result.getFirst().sortOrder());
    }

    @Test
    void getUnitImagesMapsStoredMetadataAndPresignedUrls() {
        UnitImage image = new UnitImage("units/room.jpg", true, 1, new BookableUnit());
        image.setId(7L);
        when(bookableUnitRepository.existsByPublicId(unitId)).thenReturn(true);
        when(unitImageRepository.findAllByBookableUnit_PublicIdOrderBySortOrderAsc(unitId))
                .thenReturn(List.of(image));
        doReturn("https://images.test/room").when(s3Service).createPresignedGetUrl("units/room.jpg");

        List<ImageResponseDTO> result = s3Service.getUnitImages(unitId);

        assertEquals(1, result.size());
        assertEquals(7L, result.getFirst().id());
        assertEquals("https://images.test/room", result.getFirst().url());
    }

    @Test
    void getPropertyImagesRejectsAnUnknownProperty() {
        when(propertyRepository.existsByPublicId(propertyId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> s3Service.getPropertyImages(propertyId));
        verify(propertyImageRepository, never()).findAllByProperty_PublicIdOrderBySortOrderAsc(propertyId);
    }

    @Test
    void getUnitThumbnailReturnsThePrimaryImageUrl() {
        UnitImage image = new UnitImage("units/primary.jpg", true, 1, new BookableUnit());
        when(unitImageRepository.findByBookableUnit_PublicIdAndPrimaryTrue(unitId))
                .thenReturn(Optional.of(image));
        doReturn("https://images.test/primary").when(s3Service).createPresignedGetUrl("units/primary.jpg");

        assertEquals("https://images.test/primary", s3Service.getUnitThumbnail(unitId));
    }

    @Test
    void getPropertyThumbnailRejectsAPropertyWithoutPrimaryImage() {
        when(propertyImageRepository.findByProperty_PublicIdAndPrimaryTrue(propertyId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> s3Service.getPropertyThumbnail(propertyId));
    }

    @Test
    void uploadPropertyImageRejectsAnotherOwnersPropertyBeforeUploading() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        Property property = new Property();
        property.setOwner(owner);
        when(propertyRepository.findByPublicId(propertyId)).thenReturn(Optional.of(property));
        MockMultipartFile image = new MockMultipartFile(
                "image", "room.png", "image/png", new byte[]{1, 2, 3}
        );

        assertThrows(
                AccessDeniedException.class,
                () -> s3Service.uploadPropertyImage(propertyId, image, "other@test.com")
        );
        verify(s3Client, never()).putObject(
                org.mockito.ArgumentMatchers.any(software.amazon.awssdk.services.s3.model.PutObjectRequest.class),
                org.mockito.ArgumentMatchers.any(software.amazon.awssdk.core.sync.RequestBody.class)
        );
    }

    @Test
    void uploadUnitImageRejectsEmptyFiles() {
        MockMultipartFile empty = new MockMultipartFile("image", new byte[0]);

        assertThrows(
                InvalidFileTypeException.class,
                () -> s3Service.uploadUnitImage(unitId, empty, "owner@test.com")
        );
        verify(bookableUnitRepository, never()).existsByPublicId(unitId);
    }
}
