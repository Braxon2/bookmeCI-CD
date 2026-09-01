package com.dusanbranovic.bookme.specifications;

import com.dusanbranovic.bookme.models.*;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.time.LocalDateTime;
import java.util.List;

public class BookableUnitSpecification {
    public static Specification<BookableUnit> inLocation(String city, String country) {
        return (root, query, cb) -> {
            Join<Object, Object> property = root.join("property");
            return cb.and(
                    cb.equal(cb.lower(property.get("city")), city.toLowerCase()),
                    cb.equal(cb.lower(property.get("country")), country.toLowerCase())
            );
        };
    }

    public static Specification<BookableUnit> hasCapacity(int adults, int kids) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("maxCapacity"), adults + kids),
                cb.greaterThanOrEqualTo(root.get("maxAdultCapacity"), adults),
                cb.greaterThanOrEqualTo(root.get("maxKidsCapacity"), kids)
        );
    }

    public static Specification<BookableUnit> isAvailable(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            Subquery<Long> bookingSq = query.subquery(Long.class);
            Root<Booking> booking = bookingSq.from(Booking.class);

            bookingSq.select(cb.count(booking));
            bookingSq.where(
                    cb.equal(booking.get("bookableUnit"), root),
                    cb.notEqual(booking.get("status"), BookingStatus.CANCELLED),
                    cb.lessThan(booking.get("checkIn"), end),
                    cb.greaterThan(booking.get("checkOut"), start)
            );

            return cb.greaterThan(root.get("totalUnits"), bookingSq.as(Integer.class));
        };
    }

    public static Specification<BookableUnit> hasPropertyFacilities(List<Long> facilityIds) {
        return (root, query, cb) -> {
            if (facilityIds == null || facilityIds.isEmpty()) return null; // Ignored if no facilities passed

            Predicate predicate = cb.conjunction();
            for (Long facId : facilityIds) {
                Subquery<Integer> sq = query.subquery(Integer.class);
                Root<PropertyFacility> pf = sq.from(PropertyFacility.class);
                sq.select(cb.literal(1));
                sq.where(
                        cb.equal(pf.get("property"), root.get("property")),
                        cb.equal(pf.get("facility").get("id"), facId)
                );
                predicate = cb.and(predicate, cb.exists(sq));
            }
            return predicate;
        };
    }

    public static Specification<BookableUnit> hasUnitFacilities(List<Long> facilityIds) {
        return (root, query, cb) -> {
            if (facilityIds == null || facilityIds.isEmpty()) return null;

            Predicate predicate = cb.conjunction();
            for (Long facId : facilityIds) {
                Subquery<Integer> sq = query.subquery(Integer.class);
                Root<UnitFascilityMapping> ufm = sq.from(UnitFascilityMapping.class);
                sq.select(cb.literal(1));
                sq.where(
                        cb.equal(ufm.get("bookableUnit"), root),
                        cb.equal(ufm.get("unitFascillity").get("id"), facId)
                );
                predicate = cb.and(predicate, cb.exists(sq));
            }
            return predicate;
        };
    }
}
