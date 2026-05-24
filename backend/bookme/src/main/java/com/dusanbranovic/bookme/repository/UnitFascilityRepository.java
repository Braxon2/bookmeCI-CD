package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.Fascillity;
import com.dusanbranovic.bookme.models.UnitFascillity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitFascilityRepository extends JpaRepository<UnitFascillity, Long> {
    Optional<UnitFascillity> findByName(String name);
}
