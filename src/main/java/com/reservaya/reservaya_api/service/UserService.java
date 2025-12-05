package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.CreateUserRequest;
import com.reservaya.reservaya_api.dto.UpdateUserRequest;
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
import org.springframework.util.StringUtils;
import com.reservaya.reservaya_api.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;


    public List<UserDTO> getAllUsersByInstitution(Long institutionId) {
        return userRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request, Long institutionId) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("El correo electrónico ya está registrado: " + request.getEmail());
        });

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .institution(institution)
                .build();

        User savedUser = userRepository.save(newUser);
        return mapToUserDTO(savedUser);
    }
    
    @Transactional
    public void changeUserPassword(Long userId, String oldPassword, String newPassword) {
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("La nueva contraseña no es lo suficientemente fuerte. Debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas y números.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado.")); // No debería pasar si está autenticado

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña antigua es incorrecta.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }


    @Transactional
    public Optional<UserDTO> updateUser(Long userIdToUpdate, UpdateUserRequest request, Long adminInstitutionId) {
        return userRepository.findById(userIdToUpdate)
                .filter(user -> user.getInstitution().getId().equals(adminInstitutionId))
                .map(user -> {
                    if (StringUtils.hasText(request.getName())) {
                        user.setName(request.getName());
                    }
                    if (request.getRole() != null) {
                       if (request.getRole() == Role.USER) {
                            user.setRole(request.getRole());
                       } else {
                           System.err.println("Intento de asignar rol no permitido: " + request.getRole());
                       }
                    }
                    User updatedUser = userRepository.save(user);
                    return mapToUserDTO(updatedUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long userIdToDelete, Long adminInstitutionId) {
        Optional<User> userOptional = userRepository.findById(userIdToDelete)
                .filter(user -> user.getInstitution().getId().equals(adminInstitutionId));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole() == Role.ADMIN) {
                 System.err.println("Intento de borrar a un usuario ADMIN (ID: " + userIdToDelete + ") - Operación denegada.");
                 return false;
            }
            reservationRepository.deleteByUserId(userIdToDelete);

            userRepository.deleteById(userIdToDelete);
            System.out.println("User ID " + userIdToDelete + " deleted successfully.");
            return true;
        } else {
            return false;
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