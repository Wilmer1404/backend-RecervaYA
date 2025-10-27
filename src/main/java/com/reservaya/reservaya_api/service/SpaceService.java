package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Institution; 
import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.repository.InstitutionRepository; 
import com.reservaya.reservaya_api.repository.SpaceRepository;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor 
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final InstitutionRepository institutionRepository; 

    public List<Space> getAllSpacesByInstitution(Long institutionId) {
        return spaceRepository.findByInstitutionId(institutionId);
    }

    // Obtener un espacio POR ID Y POR INSTITUCIÓN
    public Optional<Space> getSpaceByIdAndInstitution(Long id, Long institutionId) {
        return spaceRepository.findByIdAndInstitutionId(id, institutionId);
    }

    // Crear un espacio ASIGNÁNDOLE LA INSTITUCIÓN
    @Transactional
    public Space createSpace(Space space, Long institutionId) {
        // Buscar la institución para asociarla
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));
        space.setInstitution(institution); 
        // Validar otros datos de 'space' si es necesario aquí
        return spaceRepository.save(space);
    }

    // Actualizar un espacio, VERIFICANDO QUE PERTENEZCA A LA INSTITUCIÓN
    @Transactional
    public Optional<Space> updateSpace(Long id, Space spaceDetails, Long institutionId) {
        // Primero, busca el espacio existente asegurándose que pertenece a la institución correcta
        return spaceRepository.findByIdAndInstitutionId(id, institutionId).map(existingSpace -> {
            // Actualiza solo los campos permitidos
            existingSpace.setName(spaceDetails.getName());
            existingSpace.setType(spaceDetails.getType());
            existingSpace.setCapacity(spaceDetails.getCapacity());
            existingSpace.setImage(spaceDetails.getImage());
            // La institución no debería cambiar en una actualización
            return spaceRepository.save(existingSpace);
        });
    }

    // Eliminar un espacio, VERIFICANDO QUE PERTENEZCA A LA INSTITUCIÓN
    @Transactional
    public boolean deleteSpace(Long id, Long institutionId) {
        // Verifica si existe para esa institución antes de borrar
        if (spaceRepository.existsByIdAndInstitutionId(id, institutionId)) {
            // Aquí deberías añadir lógica para verificar si hay reservas activas
            // asociadas a este espacio antes de permitir borrarlo.
            // Por ahora, lo borramos directamente:
            spaceRepository.deleteById(id);
            return true;
        }
        return false; 
    }
}