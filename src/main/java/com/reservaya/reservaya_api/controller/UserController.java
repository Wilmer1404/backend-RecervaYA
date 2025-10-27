package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.model.User; // Importar User
import com.reservaya.reservaya_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize; // Para control de acceso basado en roles
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Considera añadir otros endpoints (POST, PUT, DELETE) para gestión de usuarios por el ADMIN

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Solo los ADMIN pueden listar todos los usuarios (de su institución)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Requiere habilitar @EnableMethodSecurity en SecurityConfig
    public List<UserDTO> getAllUsers(@AuthenticationPrincipal User adminUser) {
        // Lógica de filtrado por institución se añadirá después en el servicio
        Long institutionId = adminUser.getInstitution().getId();
        System.out.println("Fetching users for institution ID: " + institutionId); // Log de depuración
        // return userService.getAllUsersByInstitution(institutionId); // Llamada futura
        return userService.getAllUsers(); // Temporalmente devuelve todos
    }

    // Podrías añadir un endpoint para que un usuario obtenga su propio perfil
    // @GetMapping("/me")
    // public UserDTO getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
    //    return userService.getUserById(currentUser.getId()); // Necesitaría método en UserService
    // }
}