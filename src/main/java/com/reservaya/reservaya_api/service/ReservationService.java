// src/main/java/com/reservaya/reservaya_api/service/ReservationService.java
package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.model.enums.Role; // Importar Role
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        User user = userRepository.findById(reservation.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservation.getUser().getId()));
        Space space = spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado con ID: " + reservation.getSpace().getId()));

        if (!user.getInstitution().getId().equals(space.getInstitution().getId())) {
             throw new IllegalArgumentException("El usuario y el espacio no pertenecen a la misma institución.");
        }

        Long institutionId = user.getInstitution().getId();
        reservation.setInstitution(user.getInstitution());

        if (reservation.getStartTime() == null || reservation.getEndTime() == null || !reservation.getStartTime().isBefore(reservation.getEndTime())) {
             throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                institutionId,
                space.getId(),
                reservation.getStartTime(),
                reservation.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("El espacio ya está reservado en este horario.");
        }

        reservation.setStatus("CONFIRMED");
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservationsByInstitution(Long institutionId) {
        return reservationRepository.findByInstitutionId(institutionId);
    }

    public List<Reservation> getReservationsForUser(Long userId, Long institutionId) {
        return reservationRepository.findByUserIdAndInstitutionId(userId, institutionId);
    }

    public List<Reservation> getReservationsForSpace(Long spaceId, Long institutionId) {
        return reservationRepository.findBySpaceIdAndInstitutionId(spaceId, institutionId);
    }

     // Lógica para un USER (solo puede cancelar lo suyo)
     @Transactional
     public boolean cancelReservationAsUser(Long reservationId, Long userId, Long institutionId) {
         Optional<Reservation> reservationOpt = reservationRepository.findByIdAndInstitutionId(reservationId, institutionId);

         if (reservationOpt.isEmpty()) {
             return false; // No encontrada o no pertenece a la institución
         }

         Reservation reservation = reservationOpt.get();

         if (!reservation.getUser().getId().equals(userId)) {
             // El usuario intenta cancelar una reserva que no es suya
             throw new SecurityException("No tiene permisos para cancelar esta reserva.");
         }
         
         // Si es suya, cancelar
         reservation.setStatus("CANCELLED");
         reservationRepository.save(reservation);
         return true;
     }

     // Lógica para un ADMIN (puede cancelar cualquier reserva en su institución)
     @Transactional
     public boolean cancelReservationAsAdmin(Long reservationId, Long institutionId) {
        // Solo busca que exista en la institución
         return reservationRepository.findByIdAndInstitutionId(reservationId, institutionId)
             .map(reservation -> {
                 reservation.setStatus("CANCELLED");
                 reservationRepository.save(reservation);
                 return true;
             }).orElse(false); // No se encontró la reserva en esa institución
     }
}