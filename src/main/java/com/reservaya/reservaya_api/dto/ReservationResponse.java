package com.reservaya.reservaya_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String status;
    
    private SpaceSummary space;
    private UserSummary user;

    @Data
    @Builder
    public static class SpaceSummary {
        private Long id;
        private String name;
        private String type;
    }

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
    }
}