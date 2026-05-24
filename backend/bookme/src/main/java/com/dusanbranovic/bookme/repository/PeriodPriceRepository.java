package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PeriodPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodPriceRepository extends JpaRepository<PeriodPrice, Long> {
}
