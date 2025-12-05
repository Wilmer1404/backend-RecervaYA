package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.ChangePasswordRequest;
import com.reservaya.reservaya_api.dto.CreateUserRequest;
import com.reservaya.reservaya_api.dto.UpdateUserRequest;
import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.model.enums.Role;
import com.reservaya.reservaya_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDTO> getAllUsers(@AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        return userService.getAllUsersByInstitution(institutionId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<UserDTO> getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
         UserDTO userDto = UserDTO.builder()
                 .id(currentUser.getId())
                 .name(currentUser.getName())
                 .email(currentUser.getEmail())
                 .role(currentUser.getRole())
                 .build();
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> changeMyPassword(@RequestBody ChangePasswordRequest request, @AuthenticationPrincipal User currentUser) {
        try {
            userService.changeUserPassword(
                currentUser.getId(), 
                request.getOldPassword(), 
                request.getNewPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno."));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        try {
            UserDTO createdUser = userService.createUser(request, institutionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalStateException | IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno."));
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request, @AuthenticationPrincipal User adminUser) {
        Long adminInstitutionId = adminUser.getInstitution().getId();
        try {
             return userService.updateUser(userId, request, adminInstitutionId)
                .map(updatedUserDTO -> ResponseEntity.ok(updatedUserDTO))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno."));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        Long institutionId = currentUser.getInstitution().getId();

        // 1. Verificación de Seguridad: ¿Quién está intentando borrar?
        if (currentUser.getRole() == Role.ADMIN) {
            // ADMIN intentando borrar: No puede borrarse a sí mismo por error
            if (userId.equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "No puedes eliminar tu propia cuenta de administrador."));
            }
        } else {
            // USER intentando borrar: SOLO puede borrarse a sí mismo (darse de baja)
            if (!userId.equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "No tienes permiso para eliminar otros usuarios."));
            }
        }

        try {
            // 2. Ejecutar borrado (Service se encarga de borrar reservas primero)
            boolean deleted = userService.deleteUser(userId, institutionId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al eliminar el usuario."));
        }
    }
}