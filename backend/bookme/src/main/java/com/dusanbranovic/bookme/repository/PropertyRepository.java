package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.Property;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Property p
        WHERE p.publicId = :publicId
    """)
    Optional<Property> findByPublicIdForUpdate(
            @Param("publicId") UUID publicId
    );
}
