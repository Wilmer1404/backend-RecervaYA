// src/main/java/com/reservaya/reservaya_api/service/DashboardService.java
package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.DashboardSummaryDTO;
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Usar Lombok
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor // Usar Lombok para constructor
public class DashboardService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    // --- MÉTODO MODIFICADO ---
    // Obtener resumen PARA UNA INSTITUCIÓN
    public DashboardSummaryDTO getDashboardSummaryForInstitution(Long institutionId) {
        // Contar espacios de la institución
        long activeSpaces = spaceRepository.countByInstitutionId(institutionId); // Necesitamos añadir este método al repo

        // Contar usuarios de la institución
        long totalUsers = userRepository.countByInstitutionId(institutionId); // Necesitamos añadir este método al repo

        // Contar reservas de hoy para la institución
        LocalDate today = LocalDate.now();
        long reservationsToday = reservationRepository.countByInstitutionIdAndStartTimeBetween(
            institutionId, // Pasar institutionId
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX)
        );

        return DashboardSummaryDTO.builder()
                .activeSpaces(activeSpaces)
                .totalUsers(totalUsers)
                .reservationsToday(reservationsToday)
                .build();
    }

     // --- EL MÉTODO ANTIGUO SE ELIMINA O SE HACE PRIVADO ---
     // public DashboardSummaryDTO getDashboardSummary() { ... } // Eliminar
}