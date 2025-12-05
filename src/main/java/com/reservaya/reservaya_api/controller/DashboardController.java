package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.DashboardSummaryDTO;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.reservaya.reservaya_api.dto.AnalyticsDTO;

//Su función principal es entregar los datos estadísticos y métricas que se muestran en el panel de control (Dashboard
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN')")
    public DashboardSummaryDTO getSummary(@AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId(); 
        return dashboardService.getDashboardSummaryForInstitution(institutionId);
        //otal usuarios, Espacios activos, Reservas de hoy
    }
    
    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnalyticsDTO getAnalytics(@AuthenticationPrincipal User adminUser) {
        return dashboardService.getAnalytics(adminUser.getInstitution().getId());
        //reservas por día de la semana, ocupación, tipos de espacios más usados
    }
}