package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByName(String name);
    Optional<Institution> findByEmailDomain(String domain);
}