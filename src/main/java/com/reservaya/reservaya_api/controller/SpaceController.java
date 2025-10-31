// src/main/java/com/reservaya/reservaya_api/controller/SpaceController.java
package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.SpaceDTO; // <-- IMPORTAR DTO
import com.reservaya.reservaya_api.model.Space; // Necesario para el RequestBody
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    // --- MÉTODO MODIFICADO: Devuelve List<SpaceDTO> ---
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public List<SpaceDTO> getAllSpaces(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return spaceService.getAllSpacesByInstitution(institutionId);
    }

    // --- MÉTODO MODIFICADO: Devuelve ResponseEntity<SpaceDTO> ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<SpaceDTO> getSpaceById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return spaceService.getSpaceByIdAndInstitution(id, institutionId)
                .map(ResponseEntity::ok) // Devuelve el DTO si existe
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MÉTODO MODIFICADO: Devuelve ResponseEntity<SpaceDTO> ---
    // Recibe Space en el body, pero devuelve SpaceDTO
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpaceDTO> createSpace(@RequestBody Space space, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        try {
            // El servicio ahora devuelve DTO
            SpaceDTO createdSpaceDTO = spaceService.createSpace(space, institutionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSpaceDTO);
        } catch (IllegalArgumentException e) {
            // Puedes personalizar el mensaje de error si quieres
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
             // Loguear el error real aquí sería buena idea
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- MÉTODO MODIFICADO: Devuelve ResponseEntity<SpaceDTO> ---
    // Recibe Space en el body, devuelve SpaceDTO
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable Long id, @RequestBody Space spaceDetails, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        // El servicio ahora devuelve Optional<SpaceDTO>
        return spaceService.updateSpace(id, spaceDetails, institutionId)
                .map(ResponseEntity::ok) // Devuelve el DTO si se actualizó
                .orElse(ResponseEntity.notFound().build());
    }

    // --- (deleteSpace no necesita cambios) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        if (spaceService.deleteSpace(id, institutionId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}