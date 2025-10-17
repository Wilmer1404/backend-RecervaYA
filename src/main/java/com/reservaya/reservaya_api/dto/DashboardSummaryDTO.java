package com.reservaya.reservaya_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryDTO {
    private long activeSpaces;
    private long totalUsers;
    private long reservationsToday;
}