package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodPriceAddonRepository extends JpaRepository<PeriodPriceAddon, Long> {
}
