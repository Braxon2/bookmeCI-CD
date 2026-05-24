package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
SELECT COUNT(b) FROM Booking b
WHERE b.bookableUnit.id = :unitId
AND b.status <> 'CANCELLED'
AND :start < b.checkOut
AND :end > b.checkIn
""")
    long countOverlappingBookings(
            @Param("unitId") Long unitId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
SELECT b FROM Booking  b
WHERE b.guest.id = :userId
""")
    List<Booking> findAllByGuestIdWithUnit(@Param("userId") Long userId);
}
