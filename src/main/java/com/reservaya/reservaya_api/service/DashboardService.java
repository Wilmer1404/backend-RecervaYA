package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.DashboardSummaryDTO;
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service;
import com.reservaya.reservaya_api.dto.AnalyticsDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public DashboardSummaryDTO getDashboardSummaryForInstitution(Long institutionId) {
        long activeSpaces = spaceRepository.countByInstitutionId(institutionId); // Necesitamos añadir este método al repo
        long totalUsers = userRepository.countByInstitutionId(institutionId); // Necesitamos añadir este método al repo
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

    public AnalyticsDTO getAnalytics(Long institutionId) {
        List<Long> weeklyCounts = new ArrayList<>();
        LocalDate startOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            long count = reservationRepository.countByInstitutionIdAndStartTimeBetween(
                institutionId, date.atStartOfDay(), date.atTime(LocalTime.MAX)
            );
            weeklyCounts.add(count);
        }

        List<Object[]> typeCounts = reservationRepository.countReservationsByType(institutionId);
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeCounts) {
            if (row[0] != null) {
                typeMap.put(row[0].toString(), (Long) row[1]);
            }
        }

        long totalReservationsWeek = weeklyCounts.stream().mapToLong(Long::longValue).sum();
        double occupancy = totalReservationsWeek > 0 ? 78.5 : 0.0; // Lógica real requiere calcular horas totales disponibles

        return AnalyticsDTO.builder()
                .weeklyReservations(weeklyCounts)
                .reservationsByType(typeMap)
                .occupancyRate(occupancy)
                .build();
    }
}