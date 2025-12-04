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

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public List<Reservation> getReservationsForCalendar(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getAllReservationsByInstitution(institutionId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Reservation> getInstitutionReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getAllReservationsByInstitution(institutionId);
    }

    @GetMapping("/my-reservations")
    @PreAuthorize("hasAuthority('USER')")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return reservationService.getReservationsForUser(user.getId(), institutionId);
    }

    // --- MEJORA: Pasar 'user' al servicio ---
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation,
            @AuthenticationPrincipal User user) {
        try {
            // Pasamos 'user' aquí para que el servicio no falle
            Reservation newReservation = reservationService.createReservation(reservation, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            boolean cancelled;
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