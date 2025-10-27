package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.CreateUserRequest;
import com.reservaya.reservaya_api.dto.UpdateUserRequest;
import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*; 
import java.util.List;
import java.util.Map; // Para mensajes de error

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDTO> getAllUsers(@AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        System.out.println("API GET /users: Admin User ID " + adminUser.getId() + " fetching users for institution ID: " + institutionId);
        return userService.getAllUsersByInstitution(institutionId);
    }

    // Endpoint existente: Obtener perfil propio
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
         System.out.println("API GET /users/me: User ID " + currentUser.getId() + " fetching own profile for institution ID: " + currentUser.getInstitution().getId());
         UserDTO userDto = UserDTO.builder()
                 .id(currentUser.getId())
                 .name(currentUser.getName())
                 .email(currentUser.getEmail())
                 .role(currentUser.getRole())
                 .build();
        return ResponseEntity.ok(userDto);
    }

    // Endpoint existente: Crear un nuevo usuario (solo ADMIN)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request, @AuthenticationPrincipal User adminUser) {
        Long institutionId = adminUser.getInstitution().getId();
        System.out.println("API POST /users: Admin User ID " + adminUser.getId() + " creating user '" + request.getEmail() + "' for institution ID: " + institutionId);

        try {
            UserDTO createdUser = userService.createUser(request, institutionId);
            System.out.println(" User created successfully with ID: " + createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalStateException e) {
            System.err.println(" User creation failed (conflict): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
             System.err.println(" User creation failed (bad request): " + e.getMessage());
             return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println(" User creation failed with unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al crear el usuario."));
        }
    }

    // --- NUEVO ENDPOINT: Actualizar Usuario ---
    // Solo ADMIN puede actualizar usuarios de su institución
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request, @AuthenticationPrincipal User adminUser) {
        Long adminInstitutionId = adminUser.getInstitution().getId();
        System.out.println("API PUT /users/" + userId + ": Admin User ID " + adminUser.getId() + " attempting to update user for institution ID: " + adminInstitutionId);

        try {
             return userService.updateUser(userId, request, adminInstitutionId)
                .map(updatedUserDTO -> {
                    System.out.println(" User ID " + userId + " updated successfully.");
                    return ResponseEntity.ok(updatedUserDTO); // 200 OK con el DTO actualizado
                })
                .orElseGet(() -> {
                    System.err.println(" Update failed: User ID " + userId + " not found or doesn't belong to institution ID " + adminInstitutionId);
                    // Devolver 404 Not Found si el usuario no existe o no pertenece a la institución
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) { // Capturar otras posibles excepciones del servicio
             System.err.println(" User update failed with unexpected error: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al actualizar el usuario."));
        }
    }

    // --- NUEVO ENDPOINT: Eliminar Usuario ---
    // Solo ADMIN puede eliminar usuarios (que no sean ADMIN) de su institución
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @AuthenticationPrincipal User adminUser) {
        Long adminInstitutionId = adminUser.getInstitution().getId();
        System.out.println("API DELETE /users/" + userId + ": Admin User ID " + adminUser.getId() + " attempting to delete user for institution ID: " + adminInstitutionId);

        // Prevenir que el admin se borre a sí mismo
        if (userId.equals(adminUser.getId())) {
             System.err.println(" Delete failed: Admin User ID " + adminUser.getId() + " cannot delete themselves.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Un administrador no puede eliminarse a sí mismo.")); // 403 Forbidden
        }

        try {
            boolean deleted = userService.deleteUser(userId, adminInstitutionId);
            if (deleted) {
                System.out.println(" User ID " + userId + " deleted successfully.");
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                 System.err.println(" Delete failed: User ID " + userId + " not found, doesn't belong to institution ID " + adminInstitutionId + ", or is an ADMIN.");
                // Podría ser 404 Not Found o 403 Forbidden si intentó borrar otro admin
                return ResponseEntity.notFound().build(); // 404 es más simple
            }
        } catch (Exception e) { // Capturar otras posibles excepciones del servicio (ej. dependencias)
             System.err.println(" User deletion failed with unexpected error: " + e.getMessage());
             e.printStackTrace();
             // Podría ser un 409 Conflict si hay dependencias (reservas, etc.)
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al eliminar el usuario. Verifique dependencias."));
        }
    }
}