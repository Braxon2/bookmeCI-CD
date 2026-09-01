package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.responses.ImageResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidFileTypeException;
import com.dusanbranovic.bookme.exceptions.S3UploadException;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Property;
import com.dusanbranovic.bookme.models.PropertyImage;
import com.dusanbranovic.bookme.models.UnitImage;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.PropertyImageRepository;
import com.dusanbranovic.bookme.repository.PropertyRepository;
import com.dusanbranovic.bookme.repository.UnitImageRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;


@Service
public class S3Service {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.presigned-url-duration-minutes:30}")
    private long presignedUrlDurationMinutes;

    private final TransactionTemplate transactionTemplate;

    @Value("${app.storage-prefix:dev}")
    private String storagePrefix;

    private final PropertyRepository propertyRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final UnitImageRepository unitImageRepository;
    private final PropertyImageRepository propertyImageRepository;

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private static final Tika tika = new Tika();


    @Value("${aws.s3.bucketName}")
    private String bucketName;


    public S3Service(S3Client s3Client,
                     S3Presigner s3Presigner,
                     TransactionTemplate transactionTemplate,
                     PropertyRepository propertyRepository,
                     BookableUnitRepository bookableUnitRepository,
                     UnitImageRepository unitImageRepository,
                     PropertyImageRepository propertyImageRepository
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.transactionTemplate = transactionTemplate;
        this.propertyRepository = propertyRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.unitImageRepository = unitImageRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    public List<ImageResponseDTO> getPropertyImages(
            UUID propertyPublicId
    ) {
        if (!propertyRepository.existsByPublicId(propertyPublicId)) {
            throw new EntityNotFoundException(
                    "Property with public ID "
                            + propertyPublicId
                            + " not found"
            );
        }

        return propertyImageRepository
                .findAllByProperty_PublicIdOrderBySortOrderAsc(
                        propertyPublicId
                )
                .stream()
                .map(image -> new ImageResponseDTO(
                        image.getId(),
                        createPresignedGetUrl(image.getS3Key()),
                        image.getPrimary(),
                        image.getSortOrder()
                ))
                .toList();
    }

    public List<ImageResponseDTO> getUnitImages(
            UUID unitPublicId
    ) {
        if (!bookableUnitRepository.existsByPublicId(unitPublicId)) {
            throw new EntityNotFoundException(
                    "Bookable unit with public ID "
                            + unitPublicId
                            + " not found"
            );
        }

        return unitImageRepository
                .findAllByBookableUnit_PublicIdOrderBySortOrderAsc(
                        unitPublicId
                )
                .stream()
                .map(image -> new ImageResponseDTO(
                        image.getId(),
                        createPresignedGetUrl(image.getS3Key()),
                        image.getPrimary(),
                        image.getSortOrder()
                ))
                .toList();
    }

    public String getPropertyThumbnail(UUID propertyPublicId) {
        PropertyImage thumbnail = propertyImageRepository
                .findByProperty_PublicIdAndPrimaryTrue(
                        propertyPublicId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Property has no primary image"
                ));

        return createPresignedGetUrl(thumbnail.getS3Key());
    }

    public String getUnitThumbnail(UUID unitPublicId) {

        UnitImage thumbnail = unitImageRepository
                .findByBookableUnit_PublicIdAndPrimaryTrue(unitPublicId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Bookable unit has no primary image"
                ));

        return createPresignedGetUrl(thumbnail.getS3Key());
    }

    public String createPresignedGetUrl(String s3Key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofMinutes(
                                        presignedUrlDurationMinutes
                                )
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toExternalForm();
    }


    public ImageResponseDTO uploadPropertyImage(
            UUID propertyPublicId,
            MultipartFile file,
            String email
    ) {

        /*
         * Normal lookup here.
         * NO pessimistic lock because we're outside a transaction.
         */
        Property propertyForAuthorization = propertyRepository
                .findByPublicId(propertyPublicId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Property with public ID "
                                + propertyPublicId
                                + " not found"
                ));

        /*
         * Check ownership before uploading anything to S3.
         */
        if (!propertyForAuthorization
                .getOwner()
                .getEmail()
                .equals(email)) {

            throw new AccessDeniedException(
                    "You cannot upload images to this property"
            );
        }

        ValidatedImage validatedImage = validateImage(file);

        String key = "%s/properties/%s/%s%s".formatted(
                storagePrefix,
                propertyPublicId,
                UUID.randomUUID(),
                validatedImage.extension()
        );

        boolean uploaded = false;

        try {

            /*
             * 1. Upload to S3
             */
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(validatedImage.contentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(validatedImage.bytes())
            );

            uploaded = true;

            /*
             * 2. Start database transaction
             */
            PropertyImage savedImage = transactionTemplate.execute(status -> {

                /*
                 * NOW pessimistic locking is valid,
                 * because we're inside an active transaction.
                 */
                Property property = propertyRepository
                        .findByPublicIdForUpdate(propertyPublicId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Property with public ID "
                                        + propertyPublicId
                                        + " not found"
                        ));

                /*
                 * Re-check authorization because the entity has
                 * been fetched again inside the transaction.
                 */
                if (!property
                        .getOwner()
                        .getEmail()
                        .equals(email)) {

                    throw new AccessDeniedException(
                            "You cannot upload images to this property"
                    );
                }

                int currentMaximumOrder =
                        propertyImageRepository.findMaximumSortOrder(
                                property.getId()
                        );

                PropertyImage propertyImage = new PropertyImage();

                propertyImage.setProperty(property);
                propertyImage.setS3Key(key);
                propertyImage.setContentType(
                        validatedImage.contentType()
                );

                propertyImage.setSortOrder(
                        currentMaximumOrder + 1
                );

                propertyImage.setPrimary(
                        currentMaximumOrder == 0
                );

                return propertyImageRepository.saveAndFlush(
                        propertyImage
                );
            });

            if (savedImage == null) {
                throw new IllegalStateException(
                        "Database transaction returned no image"
                );
            }

            /*
             * 3. Generate presigned URL
             */
            return new ImageResponseDTO(
                    savedImage.getId(),
                    createPresignedGetUrl(savedImage.getS3Key()),
                    savedImage.getPrimary(),
                    savedImage.getSortOrder()
            );

        } catch (S3Exception | SdkClientException e) {

            deleteQuietly(key);

            log.error(
                    "S3 operation failed for property {} and key {}",
                    propertyPublicId,
                    key,
                    e
            );

            throw new S3UploadException(
                    "Cloud storage operation failed"
            );

        } catch (RuntimeException e) {

            /*
             * Database operation failed after successful S3 upload.
             */
            if (uploaded) {
                deleteQuietly(key);
            }

            log.error(
                    "Image persistence failed for property {} and key {}",
                    propertyPublicId,
                    key,
                    e
            );

            throw e;
        }
    }

    private void deleteQuietly(String key) {
        try {
            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(key)
            );
        } catch (RuntimeException cleanupException) {
            /*
             * At this point the object may be orphaned.
             * Log the exact key so it can be removed manually
             * or by a future cleanup job.
             */
            log.error(
                    "Failed to clean up S3 object with key {}",
                    key,
                    cleanupException
            );
        }
    }

    private ValidatedImage validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException(
                    "Image file is empty"
            );
        }

        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidFileTypeException(
                    "Could not read image file"
            );
        }

        String detectedType = tika.detect(bytes);

        String extension = switch (detectedType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new InvalidFileTypeException(
                    "Only JPEG, PNG, WebP and GIF images are allowed"
            );
        };

        return new ValidatedImage(
                bytes,
                detectedType,
                extension
        );
    }

    private record ValidatedImage(
            byte[] bytes,
            String contentType,
            String extension
    ) {
    }

    public ImageResponseDTO uploadUnitImage(
            UUID unitPublicId,
            MultipartFile file,
            String email
    ) {

        ValidatedImage validatedImage = validateImage(file);

        if (!bookableUnitRepository.existsByPublicId(unitPublicId)) {
            throw new EntityNotFoundException(
                    "Bookable unit with public ID "
                            + unitPublicId
                            + " not found"
            );
        }

        String key = "%s/units/%s/%s%s".formatted(
                storagePrefix,
                unitPublicId,
                UUID.randomUUID(),
                validatedImage.extension()
        );

        boolean uploaded = false;

        try {

            /*
             * STEP 1:
             * Upload object to S3.
             */
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(validatedImage.contentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(validatedImage.bytes())
            );

            uploaded = true;

            /*
             * STEP 2:
             * Store metadata in PostgreSQL.
             */
            UnitImage savedImage = transactionTemplate.execute(status -> {

                /*
                 * Lock the unit so that two concurrent uploads cannot both
                 * calculate primary=true / sortOrder=1.
                 */
                BookableUnit bookableUnit = bookableUnitRepository
                        .findByPublicIdForUpdate(unitPublicId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Bookable unit with public ID "
                                        + unitPublicId
                                        + " not found"
                        ));

                /*
                 * IMPORTANT:
                 * Verify that the currently authenticated user actually owns
                 * the property to which this unit belongs.
                 *
                 * Change getEmail() if principal.getName() represents
                 * username instead of email in your application.
                 */
                if (!bookableUnit
                        .getProperty()
                        .getOwner()
                        .getEmail()
                        .equals(email)) {

                    throw new AccessDeniedException(
                            "You cannot upload images to this unit"
                    );
                }

                int currentMaximumOrder =
                        unitImageRepository.findMaximumSortOrder(
                                bookableUnit.getId()
                        );

                UnitImage unitImage = new UnitImage();

                unitImage.setBookableUnit(bookableUnit);
                unitImage.setS3Key(key);

                unitImage.setSortOrder(
                        currentMaximumOrder + 1
                );

                unitImage.setPrimary(
                        currentMaximumOrder == 0
                );

                return unitImageRepository.saveAndFlush(unitImage);
            });

            if (savedImage == null) {
                throw new IllegalStateException(
                        "Database transaction returned no image"
                );
            }

            /*
             * STEP 3:
             * Generate temporary URL for frontend.
             *
             * It is NOT stored in the database.
             */
            return new ImageResponseDTO(
                    savedImage.getId(),
                    createPresignedGetUrl(savedImage.getS3Key()),
                    savedImage.getPrimary(),
                    savedImage.getSortOrder()
            );

        } catch (S3Exception | SdkClientException e) {

            /*
             * Try to remove the object in case S3's result was uncertain.
             */
            deleteQuietly(key);

            log.error(
                    "S3 operation failed for unit {} and key {}",
                    unitPublicId,
                    key,
                    e
            );

            throw new S3UploadException(
                    "Cloud storage operation failed"
            );

        } catch (RuntimeException e) {

            /*
             * S3 succeeded but PostgreSQL transaction failed.
             *
             * Compensate by deleting the uploaded S3 object.
             */
            if (uploaded) {
                deleteQuietly(key);
            }

            log.error(
                    "Image persistence failed for unit {} and key {}",
                    unitPublicId,
                    key,
                    e
            );

            throw e;
        }
    }



}
