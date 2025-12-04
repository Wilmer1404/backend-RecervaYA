package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.model.User;
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
    // --- MEJORA: Recibir User como parámetro ---
    public Reservation createReservation(Reservation reservation, User user) {
        
        // 1. Asignar el usuario que viene del controlador (autenticado)
        reservation.setUser(user);

        // 2. Validar que el objeto reservation tenga un espacio con ID
        if (reservation.getSpace() == null || reservation.getSpace().getId() == null) {
             throw new IllegalArgumentException("Debe especificar un espacio válido.");
        }

        // 3. Buscar el espacio en BD
        Space space = spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado con ID: " + reservation.getSpace().getId()));
        
        reservation.setSpace(space); // Asignar el objeto completo

        // 4. Validar que pertenezcan a la misma institución
        if (!user.getInstitution().getId().equals(space.getInstitution().getId())) {
             throw new IllegalArgumentException("El usuario y el espacio no pertenecen a la misma institución.");
        }
        reservation.setInstitution(user.getInstitution());

        // 5. Validar lógica de horas
        if (reservation.getStartTime() == null || reservation.getEndTime() == null || !reservation.getStartTime().isBefore(reservation.getEndTime())) {
             throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        // 6. Validar si ya está ocupado (Solapamiento)
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                user.getInstitution().getId(),
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

     @Transactional
     public boolean cancelReservationAsUser(Long reservationId, Long userId, Long institutionId) {
         Optional<Reservation> reservationOpt = reservationRepository.findByIdAndInstitutionId(reservationId, institutionId);
         if (reservationOpt.isEmpty()) return false;
         
         Reservation reservation = reservationOpt.get();
         if (!reservation.getUser().getId().equals(userId)) {
             throw new SecurityException("No tiene permisos para cancelar esta reserva.");
         }
         
         reservation.setStatus("CANCELLED");
         reservationRepository.save(reservation);
         return true;
     }

     @Transactional
     public boolean cancelReservationAsAdmin(Long reservationId, Long institutionId) {
         return reservationRepository.findByIdAndInstitutionId(reservationId, institutionId)
             .map(reservation -> {
                 reservation.setStatus("CANCELLED");
                 reservationRepository.save(reservation);
                 return true;
             }).orElse(false);
     }
}