package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.UnitImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitImageRepository extends JpaRepository<UnitImage, Long> {
    List<UnitImage> findAllByBookableUnit_PublicIdOrderBySortOrderAsc(
            UUID propertyPublicId
    );

    Optional<UnitImage> findByBookableUnit_PublicIdAndPrimaryTrue(
            UUID unitPublicId
    );

    @Query("""
        SELECT COALESCE(MAX(ui.sortOrder), 0)
        FROM UnitImage ui
        WHERE ui.bookableUnit.id = :unitId
    """)
    int findMaximumSortOrder(
            @Param("unitId") Long unitId
    );
}
