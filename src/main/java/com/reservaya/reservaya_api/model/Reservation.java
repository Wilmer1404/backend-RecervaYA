package com.reservaya.reservaya_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación: Muchas reservaciones pertenecen a Un usuario
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación: Muchas reservaciones apuntan a Un espacio
    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String status; // Ej: "CONFIRMED", "CANCELLED"
}