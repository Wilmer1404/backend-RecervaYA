// src/main/java/com/reservaya/reservaya_api/service/SpaceService.java
package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.SpaceDTO; // <-- IMPORTAR DTO
import com.reservaya.reservaya_api.model.Institution;
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.repository.InstitutionRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // <-- IMPORTAR Collectors

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final InstitutionRepository institutionRepository;

    // --- MÉTODO MODIFICADO: Devuelve List<SpaceDTO> ---
    public List<SpaceDTO> getAllSpacesByInstitution(Long institutionId) {
        return spaceRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToSpaceDTO) // Convertir cada Space a SpaceDTO
                .collect(Collectors.toList());
    }

    // --- MÉTODO MODIFICADO: Devuelve Optional<SpaceDTO> ---
    public Optional<SpaceDTO> getSpaceByIdAndInstitution(Long id, Long institutionId) {
        return spaceRepository.findByIdAndInstitutionId(id, institutionId)
                .map(this::mapToSpaceDTO); // Convertir a SpaceDTO si se encuentra
    }

    // --- MÉTODO MODIFICADO: Devuelve SpaceDTO ---
    @Transactional
    public SpaceDTO createSpace(Space space, Long institutionId) { // Recibe entidad, devuelve DTO
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));
        space.setInstitution(institution);
        Space savedSpace = spaceRepository.save(space);
        return mapToSpaceDTO(savedSpace); // Convertir a DTO antes de devolver
    }

    // --- MÉTODO MODIFICADO: Devuelve Optional<SpaceDTO> ---
    @Transactional
    public Optional<SpaceDTO> updateSpace(Long id, Space spaceDetails, Long institutionId) {
        return spaceRepository.findByIdAndInstitutionId(id, institutionId).map(existingSpace -> {
            existingSpace.setName(spaceDetails.getName());
            existingSpace.setType(spaceDetails.getType());
            existingSpace.setCapacity(spaceDetails.getCapacity());
            existingSpace.setImage(spaceDetails.getImage());
            Space updatedSpace = spaceRepository.save(existingSpace);
            return mapToSpaceDTO(updatedSpace); // Convertir a DTO
        });
    }

    // --- (deleteSpace no devuelve datos, no necesita DTO) ---
    @Transactional
    public boolean deleteSpace(Long id, Long institutionId) {
        if (spaceRepository.existsByIdAndInstitutionId(id, institutionId)) {
            spaceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- NUEVO MÉTODO HELPER: Para convertir Entidad a DTO ---
    private SpaceDTO mapToSpaceDTO(Space space) {
        return SpaceDTO.builder()
                .id(space.getId())
                .name(space.getName())
                .type(space.getType())
                .capacity(space.getCapacity())
                .image(space.getImage())
                // Si añadiste institutionId al DTO:
                .institutionId(space.getInstitution() != null ? space.getInstitution().getId() : null)
                .build();
    }
}