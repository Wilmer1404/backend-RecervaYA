package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        if (user.getRole().name().equals("ADMIN")) {
             return reservationService.getAllReservationsByInstitution(institutionId);
        } else {
             return reservationService.getReservationsForUser(user.getId(), institutionId);
        }
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation, @AuthenticationPrincipal User user) {
        reservation.setUser(user);
        
        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        boolean cancelled = reservationService.cancelReservation(id, user.getId(), institutionId).isPresent();

        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}