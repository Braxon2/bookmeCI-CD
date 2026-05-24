package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.ContactPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactPersonRepository extends JpaRepository<ContactPerson, Long> {
}
