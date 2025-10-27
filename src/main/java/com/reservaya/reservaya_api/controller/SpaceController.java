package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.model.User; 
import com.reservaya.reservaya_api.service.SpaceService;
import lombok.RequiredArgsConstructor; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor 
public class SpaceController {

    private final SpaceService spaceService;


    @GetMapping
    public List<Space> getAllSpaces(@AuthenticationPrincipal User user) {
        // La lógica para filtrar por user.getInstitution().getId() se añadirá después
        return spaceService.getAllSpaces(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> getSpaceById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        // La lógica para verificar que el espacio pertenece a user.getInstitution() se añadirá después
        return spaceService.getSpaceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestBody Space space, @AuthenticationPrincipal User user) {
        // La lógica para asignar user.getInstitution() al 'space' se añadirá después
        try {
            Space createdSpace = spaceService.createSpace(space); 
            return ResponseEntity.ok(createdSpace);
        } catch (Exception e) {
            // Manejo básico de errores, se puede mejorar
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Space> updateSpace(@PathVariable Long id, @RequestBody Space spaceDetails, @AuthenticationPrincipal User user) {
        // La lógica para verificar pertenencia a la institución y actualizar se añadirá después
        return spaceService.updateSpace(id, spaceDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id, @AuthenticationPrincipal User user) {
         // La lógica para verificar pertenencia a la institución antes de borrar se añadirá después
        if (spaceService.deleteSpace(id)) { // Temporalmente borra sin verificar institución
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}