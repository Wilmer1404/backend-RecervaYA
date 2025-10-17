package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.model.Space;
import com.reservaya.reservaya_api.repository.SpaceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public SpaceService(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    public List<Space> getAllSpaces() {
        return spaceRepository.findAll();
    }

    public Optional<Space> getSpaceById(Long id) {
        return spaceRepository.findById(id);
    }

    public Space createSpace(Space space) {
        return spaceRepository.save(space);
    }

    public Optional<Space> updateSpace(Long id, Space spaceDetails) {
        return spaceRepository.findById(id).map(existingSpace -> {
            existingSpace.setName(spaceDetails.getName());
            existingSpace.setType(spaceDetails.getType());
            existingSpace.setCapacity(spaceDetails.getCapacity());
            existingSpace.setImage(spaceDetails.getImage());
            return spaceRepository.save(existingSpace);
        });
    }

    public boolean deleteSpace(Long id) {
        if (spaceRepository.existsById(id)) {
            spaceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}