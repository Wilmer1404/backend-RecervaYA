package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.Space; // Asegúrate de importar Space
import com.reservaya.reservaya_api.model.User; // Asegúrate de importar User
import com.reservaya.reservaya_api.repository.InstitutionRepository; // Importar si no está
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar para transacciones

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    // Añadimos InstitutionRepository para validación futura si es necesario
    private final InstitutionRepository institutionRepository;

    // Actualizamos constructor
    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
                              SpaceRepository spaceRepository, InstitutionRepository institutionRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.institutionRepository = institutionRepository;
    }

    @Transactional // Buena práctica añadir transactional a métodos que modifican datos
    public Reservation createReservation(Reservation reservation) {
        // Validamos que el usuario, el espacio y la institución existan
        User user = userRepository.findById(reservation.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservation.getUser().getId()));
        Space space = spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado con ID: " + reservation.getSpace().getId()));

        // Aseguramos que el usuario y el espacio pertenezcan a la misma institución (IMPORTANTE para multi-tenant)
        if (!user.getInstitution().getId().equals(space.getInstitution().getId())) {
             throw new IllegalArgumentException("El usuario y el espacio no pertenecen a la misma institución.");
        }

        // Asignamos la institución a la reserva (basado en el usuario o espacio)
        reservation.setInstitution(user.getInstitution());

        // Verificamos si hay reservaciones que se superpongan en el tiempo para ESE espacio
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                space.getId(), // Usamos el ID del espacio validado
                reservation.getStartTime(),
                reservation.getEndTime());

        if (!overlapping.isEmpty()) {
            // Podrías devolver más detalles sobre la reserva conflictiva si quisieras
            throw new IllegalStateException("El espacio ya está reservado en este horario.");
        }

        reservation.setStatus("CONFIRMED"); // O quizás "PENDING" si requiere aprobación
        return reservationRepository.save(reservation);
    }

    // Método para obtener todas las reservas (Considera filtrarlas por institución más adelante)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Podrías añadir más métodos como:
    // - findReservationsByInstitution(Long institutionId)
    // - findReservationsByUserAndInstitution(Long userId, Long institutionId)
    // - cancelReservation(Long reservationId, Long userId) // Para que un usuario cancele su propia reserva
}