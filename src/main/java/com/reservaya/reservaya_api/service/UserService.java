// src/main/java/com/reservaya/reservaya_api/service/UserService.java
package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // --- MÉTODO MODIFICADO ---
    // Obtener todos los usuarios PARA UNA INSTITUCIÓN
    public List<UserDTO> getAllUsersByInstitution(Long institutionId) {
        return userRepository.findByInstitutionId(institutionId) 
                .stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        // No incluimos la contraseña ni el objeto Institution completo en el DTO
                        .build())
                .collect(Collectors.toList());
    }


    // public Optional<UserDTO> getUserById(Long userId, Long institutionId) {
    //     return userRepository.findByIdAndInstitutionId(userId, institutionId)
    //            .map(user -> UserDTO.builder()...build());
    // }
}