// src/main/java/com/reservaya/reservaya_api/dto/SpaceDTO.java
package com.reservaya.reservaya_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpaceDTO {
    private Long id;
    private String name;
    private String type;
    private int capacity;
    private String image;
    // No incluimos la relación Institution completa para evitar problemas
    // Si necesitas el ID de la institución, puedes añadirlo:
    private Long institutionId;
}