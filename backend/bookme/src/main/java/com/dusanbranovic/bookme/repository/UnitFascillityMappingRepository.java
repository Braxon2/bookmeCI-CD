package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.UnitFascilityMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitFascillityMappingRepository extends JpaRepository<UnitFascilityMapping,Long> {
}
