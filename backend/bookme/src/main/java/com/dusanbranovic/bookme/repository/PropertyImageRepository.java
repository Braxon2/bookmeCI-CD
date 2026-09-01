package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    List<PropertyImage> findAllByProperty_PublicIdOrderBySortOrderAsc(
            UUID propertyPublicId
    );

    boolean existsByProperty_Id(Long propertyId);

    @Query("""
        SELECT COALESCE(MAX(pi.sortOrder), 0)
        FROM PropertyImage pi
        WHERE pi.property.id = :propertyId
    """)
    int findMaximumSortOrder(
            @Param("propertyId") Long propertyId
    );

    Optional<PropertyImage> findByProperty_PublicIdAndPrimaryTrue(
            UUID propertyPublicId
    );
}
