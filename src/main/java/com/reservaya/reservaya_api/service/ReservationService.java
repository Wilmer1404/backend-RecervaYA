package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
            SpaceRepository spaceRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        // Validamos que el usuario y el espacio existan
        userRepository.findById(reservation.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        spaceRepository.findById(reservation.getSpace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado"));

        // ¡La lógica de negocio clave!
        // Verificamos si hay reservaciones que se superpongan en el tiempo
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                reservation.getSpace().getId(),
                reservation.getStartTime(),
                reservation.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("El espacio ya está reservado en este horario.");
        }

        reservation.setStatus("CONFIRMED");
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Podrías añadir más métodos como cancelar una reserva, obtener reservas por
    // usuario, etc.
}