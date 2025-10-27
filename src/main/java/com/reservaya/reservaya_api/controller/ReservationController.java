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
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // ENDPOINT PARA ADMIN: Obtener todas las reservas de la institución
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Reservation> getInstitutionReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getAllReservationsByInstitution(institutionId);
    }

    // ENDPOINT PARA USER: Obtener solo mis reservas
    @GetMapping("/my-reservations")
    @PreAuthorize("hasAuthority('USER')")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getReservationsForUser(user.getId(), institutionId);
    }


    @PostMapping
    @PreAuthorize("hasAuthority('USER')") // Solo USER puede CREAR reservas para sí mismo
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation, @AuthenticationPrincipal User user) {
        // Asegurarse de que la reserva se asigna al usuario autenticado
        reservation.setUser(user);
        
        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
        } catch (IllegalStateException e) {
            // Conflicto de horario
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Datos inválidos (ej. usuario/espacio no existen, horas incorrectas)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al crear la reserva."));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')") // Ambos pueden cancelar
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal User user) {
        
        boolean cancelled;
        
        try {
            if (user.getRole() == Role.ADMIN) {
                // Admin puede cancelar cualquier reserva en su institución
                cancelled = reservationService.cancelReservationAsAdmin(id, user.getInstitution().getId());
            } else {
                // User solo puede cancelar sus propias reservas
                cancelled = reservationService.cancelReservationAsUser(id, user.getId(), user.getInstitution().getId());
            }

            if (cancelled) {
                return ResponseEntity.noContent().build(); // 204 OK
            } else {
                // 404 Not Found (La reserva no existe o no pertenece a la institución)
                return ResponseEntity.notFound().build();
            }
        } catch (SecurityException e) {
             // 403 Forbidden (User intentando borrar reserva ajena)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}