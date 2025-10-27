package com.reservaya.reservaya_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String institutionName;
    private String institutionType; 
    private String institutionEmailDomain; 

    private String adminName;
    private String adminEmail;
    private String adminPassword;
}