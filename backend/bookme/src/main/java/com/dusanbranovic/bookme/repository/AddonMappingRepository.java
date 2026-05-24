package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.AddonMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddonMappingRepository extends JpaRepository<AddonMapping, Long> {

    @Query("SELECT adm " +
            "FROM AddonMapping adm " +
            "WHERE adm.bookableUnit.id = :unitId " +
            "AND" +
            " adm.addon.id = :addonId"
    )
    Optional<AddonMapping> findByAddonAndUnitID(
            @Param("unitId") Long unitId,
            @Param("addonId") Long addonId
    );
}
