package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.service.SpaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    public List<Space> getAllSpaces() {
        return spaceService.getAllSpaces();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> getSpaceById(@PathVariable Long id) {
        return spaceService.getSpaceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Space createSpace(@RequestBody Space space) {
        return spaceService.createSpace(space);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Space> updateSpace(@PathVariable Long id, @RequestBody Space spaceDetails) {
        return spaceService.updateSpace(id, spaceDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        if (spaceService.deleteSpace(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}