// src/main/java/com/reservaya/reservaya_api/repository/ReservationRepository.java
package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Importar Param
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // --- CONSULTA MODIFICADA ---
    // Ahora incluye el institutionId en la cláusula WHERE
    @Query("SELECT r FROM Reservation r WHERE r.institution.id = :institutionId AND r.space.id = :spaceId AND r.status <> 'CANCELLED' AND " +
           "(r.startTime < :endTime AND r.endTime > :startTime)")
    List<Reservation> findOverlappingReservations(
            @Param("institutionId") Long institutionId, // Añadir parámetro
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // --- MÉTODO MODIFICADO PARA DASHBOARD ---
    // Contar reservas entre fechas PARA UNA INSTITUCIÓN
    long countByInstitutionIdAndStartTimeBetween(Long institutionId, LocalDateTime start, LocalDateTime end);

    // --- NUEVOS MÉTODOS ---
    // Encontrar todas las reservas para una institución específica
    List<Reservation> findByInstitutionId(Long institutionId);

    // Encontrar reservas para un usuario específico dentro de una institución
    List<Reservation> findByUserIdAndInstitutionId(Long userId, Long institutionId);

    // Encontrar reservas para un espacio específico dentro de una institución
    List<Reservation> findBySpaceIdAndInstitutionId(Long spaceId, Long institutionId);
}