package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; 
import java.util.Optional;

@Repository 
public interface SpaceRepository extends JpaRepository<Space, Long> {


    List<Space> findByInstitutionId(Long institutionId);

    // Encontrar un espacio específico por su ID y el ID de su institución
    Optional<Space> findByIdAndInstitutionId(Long id, Long institutionId);

    // Verificar si existe un espacio por ID y el ID de su institución
    boolean existsByIdAndInstitutionId(Long id, Long institutionId);

    long countByInstitutionId(Long institutionId);
}