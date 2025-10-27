package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.CreateUserRequest;
import com.reservaya.reservaya_api.dto.UpdateUserRequest; // Necesitaremos crear este DTO
import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.model.Institution;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.model.enums.Role;
import com.reservaya.reservaya_api.repository.InstitutionRepository;
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Para verificar strings no vacíos

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    // --- MÉTODOS EXISTENTES ---

    public List<UserDTO> getAllUsersByInstitution(Long institutionId) {
        return userRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request, Long institutionId) {
        // Validación de email existente (global)
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("El correo electrónico ya está registrado: " + request.getEmail());
        });

        // Obtener la institución
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));

        // Crear el nuevo usuario
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Rol USER por defecto al crear desde admin
                .institution(institution)
                .build();

        // Guardar y mapear a DTO
        User savedUser = userRepository.save(newUser);
        return mapToUserDTO(savedUser);
    }

    // --- NUEVOS MÉTODOS (NECESARIOS PARA GESTIÓN ADMIN) ---

    @Transactional
    public Optional<UserDTO> updateUser(Long userIdToUpdate, UpdateUserRequest request, Long adminInstitutionId) {
        // Busca al usuario asegurándose que pertenece a la institución del admin
        return userRepository.findById(userIdToUpdate)
                .filter(user -> user.getInstitution().getId().equals(adminInstitutionId)) // Validar pertenencia
                .map(user -> {
                    // Actualizar campos permitidos (ej. nombre, rol si se permite cambiarlo)
                    if (StringUtils.hasText(request.getName())) { // Solo actualiza si el nombre no es nulo/vacío
                        user.setName(request.getName());
                    }
                    if (request.getRole() != null) { // Si se permite cambiar el rol
                       // ¡Cuidado! No permitir cambiar a ADMIN o validar lógicas
                       if (request.getRole() == Role.USER) { // Ejemplo: solo permitir asignar USER
                            user.setRole(request.getRole());
                       } else {
                           // Podrías lanzar una excepción si intentan asignar ADMIN
                           System.err.println("Intento de asignar rol no permitido: " + request.getRole());
                       }
                    }
                    // Podrías añadir lógica para cambiar email si es necesario (requiere validación de unicidad)
                    // NO actualizamos la contraseña aquí (se haría en otro método/endpoint)

                    User updatedUser = userRepository.save(user);
                    return mapToUserDTO(updatedUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long userIdToDelete, Long adminInstitutionId) {
        // Busca al usuario asegurándose que pertenece a la institución del admin
        Optional<User> userOptional = userRepository.findById(userIdToDelete)
                .filter(user -> user.getInstitution().getId().equals(adminInstitutionId)); // Validar pertenencia

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // ¡IMPORTANTE! No permitir que un admin se borre a sí mismo
            // O que borre al último admin de la institución.
            if (user.getRole() == Role.ADMIN) {
                 System.err.println("Intento de borrar a un usuario ADMIN (ID: " + userIdToDelete + ") - Operación denegada.");
                 return false; // O lanzar excepción
            }

            // Aquí deberías añadir lógica para verificar si el usuario tiene dependencias
            // (ej. reservas activas) antes de borrarlo. Podrías anonimizarlo en lugar de borrar.
            // Por ahora, lo borramos directamente:
            userRepository.deleteById(userIdToDelete);
            System.out.println("User ID " + userIdToDelete + " deleted successfully.");
            return true;
        } else {
             System.err.println("User ID " + userIdToDelete + " not found or does not belong to institution ID " + adminInstitutionId);
            return false; // No encontrado o no pertenece a la institución
        }
    }



    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}