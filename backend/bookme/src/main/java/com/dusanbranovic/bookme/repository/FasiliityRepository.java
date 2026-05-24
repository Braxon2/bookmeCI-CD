package com.dusanbranovic.bookme.repository;


 import com.dusanbranovic.bookme.models.Fascillity;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

 import java.util.Optional;

@Repository
public interface FasiliityRepository extends JpaRepository<Fascillity, Long> {
    Optional<Fascillity> findByName(String name);
}
