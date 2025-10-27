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
        // Lógica de filtrado por institución se añadirá después
        return reservationService.getAllReservations(); 
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation, @AuthenticationPrincipal User user) {
        // Asignar el usuario que hace la reserva y validar/asignar institución en el servicio
        reservation.setUser(user); 

        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.ok(newReservation);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Manejo de conflictos (ej. espacio ocupado) o datos inválidos
             System.err.println(" Reservation creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); 
        } catch (Exception e) {
             System.err.println(" Reservation failed with unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); 
        }
    }

}