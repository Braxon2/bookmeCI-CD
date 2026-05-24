package com.dusanbranovic.bookme.service;

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
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;


@Service
public class S3Service {

    private final S3Client s3Client;

    private final PropertyRepository propertyRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final UnitImageRepository unitImageRepository;
    private final PropertyImageRepository propertyImageRepository;

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private static final Tika tika = new Tika();


    @Value("${aws.s3.bucketName}")
    private String bucketName;


    public S3Service(S3Client s3Client,
                     PropertyRepository propertyRepository,
                     BookableUnitRepository bookableUnitRepository,
                     UnitImageRepository unitImageRepository,
                     PropertyImageRepository propertyImageRepository
    ) {
        this.s3Client = s3Client;
        this.propertyRepository = propertyRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.unitImageRepository = unitImageRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    public String uploadPropertyImage(Long pid, MultipartFile file){


        if(file.isEmpty()){
            log.error("File is empty");
            throw new InvalidFileTypeException("File is empty");
        }

        Property property = propertyRepository.findById(pid)
                .orElseThrow(() -> {
                    log.error("Property not found");
                    return new EntityNotFoundException("Property with id " + pid + " not found");
                });

        log.info("Property successfully fetched");

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if(originalFilename != null && originalFilename.contains(".")){
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }


        String key = "properties/" + pid + "/" + UUID.randomUUID() + fileExtension;

        try{
            String detectedType = tika.detect(file.getInputStream());

            if(!detectedType.startsWith("image/")){
                throw new InvalidFileTypeException("Only image files are allowed");
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.
                    builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(),file.getSize());

            s3Client.putObject(putObjectRequest, requestBody);

            String url = s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .toExternalForm();

            PropertyImage propertyImage = new PropertyImage();
            propertyImage.setProperty(property);
            propertyImage.setContentType(file.getContentType());
            propertyImage.setS3Key(key);
            propertyImage.setUrl(url);

            List<PropertyImage> propertyImages = property.getImages();
            if(propertyImages == null || propertyImages.isEmpty()){
                propertyImage.setPrimary(true);
                propertyImage.setSortOrder(1);
            }else{
                propertyImage.setPrimary(false);
                propertyImage.setSortOrder(propertyImages.size() + 1);
            }


            propertyImageRepository.save(propertyImage);

            propertyImages.add(propertyImage);

            log.info("Image successfully added to property");

            return url;
        } catch (IOException e) {
            log.error("S3 Upload failed for property {}", pid, e);
            throw new S3UploadException("Cloud storage upload failed");
        }

    }

    public String uploadUnitImage(Long uid, MultipartFile file){

        if(file.isEmpty()){
            log.error("File is empty");
            throw new InvalidFileTypeException("File is empty");
        }

        BookableUnit bookableUnit = bookableUnitRepository.findById(uid)
                .orElseThrow(() -> {
                    log.error("Unit not found");
                    return new EntityNotFoundException("Unit with id " + uid + " not found");
                });

        log.info("Unit successfully fetched");

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if(originalFileName != null && originalFileName.contains(".")){
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String key = "units/" + uid + "/" + UUID.randomUUID()  + fileExtension;

        try{
            String detectedType = tika.detect(file.getInputStream());

            if(!detectedType.startsWith("image/")){
                throw new InvalidFileTypeException("Only image files are allowed");
            }



            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

            s3Client.putObject(putObjectRequest, requestBody);

            String url = s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .toExternalForm();

            UnitImage unitImage = new UnitImage();
            unitImage.setBookableUnit(bookableUnit);
            unitImage.setS3Key(key);
            unitImage.setUrl(url);

            List<UnitImage> unitImages = bookableUnit.getImages();
            if(unitImages == null || unitImages.isEmpty()){
                unitImage.setPrimary(true);
                unitImage.setSortOrder(1);
            }else{
                unitImage.setPrimary(false);
                unitImage.setSortOrder(unitImages.size() + 1);
            }

            unitImageRepository.save(unitImage);

            unitImages.add(unitImage);
            log.info("Image successfully added to unit");

            return url;

        } catch (IOException e) {
            log.error("S3 Upload failed for unit {}", uid, e);
            throw new S3UploadException("Cloud storage upload failed");
        }

    }



}
