package com.reservaya.reservaya_api.service; 

import com.reservaya.reservaya_api.dto.DashboardSummaryDTO;
import com.reservaya.reservaya_api.repository.ReservationRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class DashboardService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public DashboardService(SpaceRepository spaceRepository, UserRepository userRepository, ReservationRepository reservationRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        long activeSpaces = spaceRepository.count();
        long totalUsers = userRepository.count();
        
        LocalDate today = LocalDate.now();
        long reservationsToday = reservationRepository.countByStartTimeBetween(
            today.atStartOfDay(), 
            today.atTime(LocalTime.MAX)
        );

        return DashboardSummaryDTO.builder()
                .activeSpaces(activeSpaces)
                .totalUsers(totalUsers)
                .reservationsToday(reservationsToday)
                .build();
    }
}