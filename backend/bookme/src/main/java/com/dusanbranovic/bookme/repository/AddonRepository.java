package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {
    Optional<Addon> findByName(String name);
}
