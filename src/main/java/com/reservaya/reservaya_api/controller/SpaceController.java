package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // Importar HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importar para permisos
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
        Long institutionId = user.getInstitution().getId();
        return spaceService.getAllSpacesByInstitution(institutionId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> getSpaceById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long institutionId = user.getInstitution().getId();
        return spaceService.getSpaceByIdAndInstitution(id, institutionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Space> createSpace(@RequestBody Space space, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        try {
            Space createdSpace = spaceService.createSpace(space, institutionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSpace);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Space> updateSpace(@PathVariable Long id, @RequestBody Space spaceDetails, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        return spaceService.updateSpace(id, spaceDetails, institutionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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