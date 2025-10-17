package com.reservaya.reservaya_api.repository;

import com.reservaya.reservaya_api.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.space.id = :spaceId AND r.status <> 'CANCELLED' AND " +
           "(r.startTime < :endTime AND r.endTime > :startTime)")
    List<Reservation> findOverlappingReservations(Long spaceId, LocalDateTime startTime, LocalDateTime endTime);

    // Método para el dashboard
    long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}