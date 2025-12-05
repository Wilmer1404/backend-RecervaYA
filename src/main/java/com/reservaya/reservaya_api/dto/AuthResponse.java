package com.reservaya.reservaya_api.dto;

import com.reservaya.reservaya_api.model.enums.Role; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Role userRole; 
    private Long institutionId; 
    private Long userId;
    private String userName;
}