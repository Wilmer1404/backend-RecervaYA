package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.SpaceDTO;
import com.reservaya.reservaya_api.model.Institution;
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.repository.InstitutionRepository;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final InstitutionRepository institutionRepository;

    // --- Devuelve la lista convertida a DTOs ---
    public List<SpaceDTO> getAllSpacesByInstitution(Long institutionId) {
        return spaceRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToSpaceDTO)
                .collect(Collectors.toList());
    }

    // --- Devuelve un Optional con el DTO ---
    public Optional<SpaceDTO> getSpaceByIdAndInstitution(Long id, Long institutionId) {
        return spaceRepository.findByIdAndInstitutionId(id, institutionId)
                .map(this::mapToSpaceDTO);
    }

    // --- CREAR: Ahora recibe SpaceDTO directamente ---
    @Transactional
    public SpaceDTO createSpace(SpaceDTO request, Long institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));

        // Construimos la entidad usando los datos del DTO, incluyendo los horarios
        Space space = Space.builder()
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .image(request.getImage())
                .institution(institution)
                // --- CAMPOS DE HORARIO ---
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                // -------------------------
                .build();

        Space savedSpace = spaceRepository.save(space);
        return mapToSpaceDTO(savedSpace);
    }

    // --- ACTUALIZAR: Ahora recibe SpaceDTO para tomar los horarios ---
    @Transactional
    public Optional<SpaceDTO> updateSpace(Long id, SpaceDTO spaceDetails, Long institutionId) {
        return spaceRepository.findByIdAndInstitutionId(id, institutionId).map(existingSpace -> {
            existingSpace.setName(spaceDetails.getName());
            existingSpace.setType(spaceDetails.getType());
            existingSpace.setCapacity(spaceDetails.getCapacity());
            existingSpace.setImage(spaceDetails.getImage());
            
            // --- ACTUALIZAR HORARIOS ---
            existingSpace.setOpeningTime(spaceDetails.getOpeningTime());
            existingSpace.setClosingTime(spaceDetails.getClosingTime());
            // ---------------------------

            Space updatedSpace = spaceRepository.save(existingSpace);
            return mapToSpaceDTO(updatedSpace);
        });
    }

    // --- ELIMINAR ---
    @Transactional
    public boolean deleteSpace(Long id, Long institutionId) {
        if (spaceRepository.existsByIdAndInstitutionId(id, institutionId)) {
            spaceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- MAPPER: Convierte de Entidad a DTO ---
    private SpaceDTO mapToSpaceDTO(Space space) {
        return SpaceDTO.builder()
                .id(space.getId())
                .name(space.getName())
                .type(space.getType())
                .capacity(space.getCapacity())
                .image(space.getImage())
                .institutionId(space.getInstitution() != null ? space.getInstitution().getId() : null)
                // --- INCLUIR HORARIOS EN LA RESPUESTA ---
                .openingTime(space.getOpeningTime())
                .closingTime(space.getClosingTime())
                // ----------------------------------------
                .build();
    }
}