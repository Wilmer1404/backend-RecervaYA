package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Le indica a Spring que esta es una interfaz de repositorio
public interface SpaceRepository extends JpaRepository<Space, Long> {
    // ¡Eso es todo!
}