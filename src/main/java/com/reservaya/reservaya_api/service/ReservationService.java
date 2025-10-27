// src/main/java/com/reservaya/reservaya_api/service/ReservationService.java
package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Usar Lombok
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Usar Lombok para constructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    // --- MÉTODO CREATE MODIFICADO ---
    @Transactional
    public Reservation createReservation(Reservation reservation) { // Ya recibe user y space desde el controller
        // Re-validar usuario y espacio (aunque ya vengan asignados) por seguridad
        User user = userRepository.findById(reservation.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservation.getUser().getId()));
        Space space = spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado con ID: " + reservation.getSpace().getId()));

        // Validar que usuario y espacio pertenecen a la MISMA institución
        if (!user.getInstitution().getId().equals(space.getInstitution().getId())) {
             throw new IllegalArgumentException("El usuario y el espacio no pertenecen a la misma institución.");
        }
        // Validar que la reserva se está creando para la institución correcta

        Long institutionId = user.getInstitution().getId();
        reservation.setInstitution(user.getInstitution()); // Asignar institución

        // Validar horarios (Start < End)
        if (reservation.getStartTime() == null || reservation.getEndTime() == null || !reservation.getStartTime().isBefore(reservation.getEndTime())) {
             throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        // Verificamos solapamientos DENTRO DE LA INSTITUCIÓN
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                institutionId, // Pasar institutionId
                space.getId(),
                reservation.getStartTime(),
                reservation.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("El espacio ya está reservado en este horario.");
        }

        reservation.setStatus("CONFIRMED"); // O PENDING
        return reservationRepository.save(reservation);
    }

    // --- MÉTODO GETALL MODIFICADO ---
    // Obtener todas las reservas PARA UNA INSTITUCIÓN
    public List<Reservation> getAllReservationsByInstitution(Long institutionId) {
        return reservationRepository.findByInstitutionId(institutionId);
    }

    // --- NUEVOS MÉTODOS (EJEMPLOS) ---

    // Obtener reservas de un usuario específico DENTRO DE SU INSTITUCIÓN
    public List<Reservation> getReservationsForUser(Long userId, Long institutionId) {
        // Validar que el usuario pertenece a la institución si es necesario
        return reservationRepository.findByUserIdAndInstitutionId(userId, institutionId);
    }

    // Obtener reservas de un espacio específico DENTRO DE SU INSTITUCIÓN
     public List<Reservation> getReservationsForSpace(Long spaceId, Long institutionId) {
        // Validar que el espacio pertenece a la institución si es necesario
        return reservationRepository.findBySpaceIdAndInstitutionId(spaceId, institutionId);
    }

     // Cancelar una reserva (ejemplo básico, necesita más validaciones)
     @Transactional
     public Optional<Reservation> cancelReservation(Long reservationId, Long userId, Long institutionId) {
         return reservationRepository.findById(reservationId)
             // Asegurarse que la reserva pertenece al usuario Y a la institución
             .filter(res -> res.getUser().getId().equals(userId) && res.getInstitution().getId().equals(institutionId))
             .map(reservation -> {

                 reservation.setStatus("CANCELLED");
                 return reservationRepository.save(reservation);
             });
     }
}