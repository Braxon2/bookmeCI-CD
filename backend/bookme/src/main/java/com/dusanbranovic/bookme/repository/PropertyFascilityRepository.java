package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PropertyFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyFascilityRepository extends JpaRepository<PropertyFacility, Long> {
}
