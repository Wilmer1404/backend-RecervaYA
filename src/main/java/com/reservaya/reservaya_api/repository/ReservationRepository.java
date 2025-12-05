package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; 

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.institution.id = :institutionId AND r.space.id = :spaceId AND r.status <> 'CANCELLED' AND " +
           "(r.startTime < :endTime AND r.endTime > :startTime)")
    List<Reservation> findOverlappingReservations(
            @Param("institutionId") Long institutionId,
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    long countByInstitutionIdAndStartTimeBetween(Long institutionId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByInstitutionId(Long institutionId);

    List<Reservation> findByUserIdAndInstitutionId(Long userId, Long institutionId);

    List<Reservation> findBySpaceIdAndInstitutionId(Long spaceId, Long institutionId);
    
    Optional<Reservation> findByIdAndInstitutionId(Long id, Long institutionId);

    @Query("SELECT s.type, COUNT(r) FROM Reservation r JOIN r.space s WHERE r.institution.id = :institutionId GROUP BY s.type")
    List<Object[]> countReservationsByType(@Param("institutionId") Long institutionId);

    // --- NUEVO MÉTODO CRÍTICO: Eliminar reservas por ID de espacio ---
    void deleteBySpaceId(Long spaceId);
}