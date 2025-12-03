package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.SpaceDTO; 
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

    // --- Devuelve todos los espacios asociados a esa institución. ---
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public List<SpaceDTO> getAllSpaces(@AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return spaceService.getAllSpacesByInstitution(institutionId);
    }

    // --- obtener espacios por ID ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<SpaceDTO> getSpaceById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return spaceService.getSpaceByIdAndInstitution(id, institutionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- crear un espacio ---
    // CORREGIDO: Recibe SpaceDTO en el body
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpaceDTO> createSpace(@RequestBody SpaceDTO spaceDTO, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        try {
            // Pasamos el DTO directamente al servicio
            SpaceDTO createdSpaceDTO = spaceService.createSpace(spaceDTO, institutionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSpaceDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace(); // Es útil imprimir el error en consola para depurar
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Actualizar espacio ---
    // CORREGIDO: Recibe SpaceDTO en el body
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable Long id, @RequestBody SpaceDTO spaceDetailsDTO, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        
        // Pasamos el DTO directamente al servicio
        return spaceService.updateSpace(id, spaceDetailsDTO, institutionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- eliminar espacios ---
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