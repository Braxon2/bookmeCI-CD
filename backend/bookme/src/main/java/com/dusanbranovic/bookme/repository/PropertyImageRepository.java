package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
}
