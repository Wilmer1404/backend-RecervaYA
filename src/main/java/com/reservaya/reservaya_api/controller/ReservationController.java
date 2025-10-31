package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.model.enums.Role;
import com.reservaya.reservaya_api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations") // Ruta base
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // --- INICIO DE LA SOLUCIÓN AL ERROR 405 ---
    /**
     * Endpoint para el Calendario (GET /api/v1/reservations)
     * * El frontend (probablemente un componente de calendario) está llamando a
     * esta
     * ruta base (GET en la raíz) para obtener todas las reservas y pintar los
     * horarios ocupados.
     * * Se requiere autorización de USER o ADMIN.
     * Devuelve todas las reservas de la institución a la que pertenece el usuario.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public List<Reservation> getReservationsForCalendar(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        // Reutilizamos el servicio que ya obtiene todas las reservas de la institución
        return reservationService.getAllReservationsByInstitution(institutionId);
    }
    // --- FIN DE LA SOLUCIÓN ---

    // ENDPOINT PARA ADMIN: Obtener todas las reservas (GET
    // /api/v1/reservations/all)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Reservation> getInstitutionReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getAllReservationsByInstitution(institutionId);
    }

    // ENDPOINT PARA USER: Obtener solo mis reservas (GET
    // /api/v1/reservations/my-reservations)
    @GetMapping("/my-reservations")
    @PreAuthorize("hasAuthority('USER')")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getReservationsForUser(user.getId(), institutionId);
    }

    // CREAR RESERVA (POST /api/v1/reservations)
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation,
            @AuthenticationPrincipal User user) {
        reservation.setUser(user);

        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al crear la reserva."));
        }
    }

    // CANCELAR RESERVA (DELETE /api/v1/reservations/{id})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal User user) {

        boolean cancelled;

        try {
            if (user.getRole() == Role.ADMIN) {
                cancelled = reservationService.cancelReservationAsAdmin(id, user.getInstitution().getId());
            } else {
                cancelled = reservationService.cancelReservationAsUser(id, user.getId(), user.getInstitution().getId());
            }

            if (cancelled) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}