package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.ReservationResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    @Transactional
    public Reservation createReservation(Reservation reservation, User user) {
        reservation.setUser(user);

        if (reservation.getSpace() == null || reservation.getSpace().getId() == null) {
             throw new IllegalArgumentException("Debe especificar un espacio válido.");
        }

        Space space = spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado con ID: " + reservation.getSpace().getId()));
        
        reservation.setSpace(space);

        if (!user.getInstitution().getId().equals(space.getInstitution().getId())) {
             throw new IllegalArgumentException("El usuario y el espacio no pertenecen a la misma institución.");
        }
        reservation.setInstitution(user.getInstitution());

        if (reservation.getStartTime() == null || reservation.getEndTime() == null || !reservation.getStartTime().isBefore(reservation.getEndTime())) {
             throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

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

    // --- MÉTODOS ACTUALIZADOS PARA DEVOLVER DTO ---

    public List<ReservationResponse> getAllReservationsByInstitution(Long institutionId) {
        return reservationRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsForUser(Long userId, Long institutionId) {
        return reservationRepository.findByUserIdAndInstitutionId(userId, institutionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Mapper auxiliar
    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus())
                .space(ReservationResponse.SpaceSummary.builder()
                        .id(r.getSpace().getId())
                        .name(r.getSpace().getName())
                        .type(r.getSpace().getType())
                        .build())
                .user(ReservationResponse.UserSummary.builder()
                        .id(r.getUser().getId())
                        .name(r.getUser().getName())
                        .email(r.getUser().getEmail())
                        .build())
                .build();
    }

    // ------------------------------------------------

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