// src/main/java/com/reservaya/reservaya_api/repository/UserRepository.java
package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Importar List
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Encontrar todos los usuarios para una institución específica
    List<User> findByInstitutionId(Long institutionId);

    long countByInstitutionId(Long institutionId);


    // Optional<User> findByIdAndInstitutionId(Long id, Long institutionId);
}