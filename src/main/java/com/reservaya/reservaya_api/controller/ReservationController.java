package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Reservation;
import com.reservaya.reservaya_api.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.ok(newReservation);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Si la lógica de negocio falla (ej. espacio ocupado), devolvemos un error 409
            // Conflict
            return ResponseEntity.status(409).body(null);
        }
    }
}