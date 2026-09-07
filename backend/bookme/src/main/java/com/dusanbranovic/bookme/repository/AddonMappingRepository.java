package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.AddonMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddonMappingRepository
        extends JpaRepository<AddonMapping, Long> {

    // Used for owner/admin operations:
    // add, remove, change billing type, add prices.
    @Query("""
        SELECT am
        FROM AddonMapping am
        WHERE am.bookableUnit.publicId = :unitId
        AND am.addon.id = :addonId
        AND am.activeUntil IS NULL
    """)
    Optional<AddonMapping> findActiveByAddonAndUnit(
            @Param("unitId") UUID unitId,
            @Param("addonId") Long addonId
    );


    // All addons available for the complete requested booking period.
    @Query("""
        SELECT am
        FROM AddonMapping am
        WHERE am.bookableUnit.publicId = :unitId
        AND am.activeFrom <= :startDate
        AND (
            am.activeUntil IS NULL
            OR am.activeUntil >= :endDate
        )
    """)
    List<AddonMapping> findAvailableAddonsForPeriod(
            @Param("unitId") UUID unitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // One specific addon must be available for the entire booking period.
    @Query("""
        SELECT am
        FROM AddonMapping am
        WHERE am.bookableUnit.publicId = :unitId
        AND am.addon.id = :addonId
        AND am.activeFrom <= :startDate
        AND (
            am.activeUntil IS NULL
            OR am.activeUntil >= :endDate
        )
    """)
    Optional<AddonMapping> findAvailableAddonForPeriod(
            @Param("unitId") UUID unitId,
            @Param("addonId") Long addonId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // Current addons. Useful for management/info pages without booking dates.
    List<AddonMapping> findByBookableUnit_PublicIdAndActiveUntilIsNull(
            UUID unitId
    );


    // Historical mappings. Keep only if you plan to expose history.
    List<AddonMapping>
    findAllByBookableUnit_PublicIdAndAddon_IdOrderByActiveFromDesc(
            UUID unitId,
            Long addonId
    );
}
