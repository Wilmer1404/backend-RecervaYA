package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.DashboardSummaryDTO;
import com.reservaya.reservaya_api.model.User; 
import com.reservaya.reservaya_api.service.DashboardService;
import lombok.RequiredArgsConstructor; 
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor 
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary(@AuthenticationPrincipal User user) {
        // La lógica para filtrar por user.getInstitution().getId() se añadirá después
        Long institutionId = user.getInstitution().getId();
        System.out.println("Fetching dashboard summary for institution ID: " + institutionId); // Log de depuración
        // return dashboardService.getDashboardSummaryForInstitution(institutionId); // Llamada futura
         return dashboardService.getDashboardSummary(); // Temporalmente devuelve global
    }
}