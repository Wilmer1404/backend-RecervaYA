package com.reservaya.reservaya_api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsDTO {
    private List<Long> weeklyReservations; // Cantidad de reservas por día (Lun-Dom)
    private Map<String, Long> reservationsByType; // Ej: "Laboratorio": 5
    private Double occupancyRate; // Porcentaje de ocupación
}