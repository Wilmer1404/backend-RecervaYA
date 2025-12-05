package com.reservaya.reservaya_api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsDTO {
    private List<Long> weeklyReservations; 
    private Map<String, Long> reservationsByType; 
    private Double occupancyRate; 
}