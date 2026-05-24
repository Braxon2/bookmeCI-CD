package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyTypeRepository extends JpaRepository<PropertyType, Long> {
    Optional<PropertyType> findByName(String name);
}
