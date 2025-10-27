package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.AuthResponse;
import com.reservaya.reservaya_api.dto.LoginRequest;
import com.reservaya.reservaya_api.dto.RegisterRequest; 
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.AuthService;
import lombok.RequiredArgsConstructor; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor 
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> registerInstitutionAndAdmin(@RequestBody RegisterRequest request) {
        System.out.println(" Registration attempt for institution: " + request.getInstitutionName() + " with admin email: " + request.getAdminEmail());
        try {
            User registeredUser = authService.register(request);
            System.out.println(" Institution and Admin User registered successfully with User ID: " + registeredUser.getId());
            // Devolver 200 OK con el usuario creado en el cuerpo
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalStateException e) {
             System.err.println(" Registration failed: " + e.getMessage());
             // Devolver 409 Conflict si el nombre/email ya existe
             // Podríamos usar GlobalExceptionHandler también, pero esto es más específico aquí
             return ResponseEntity.status(409).body(null); // O devolver un DTO de error
        } catch (Exception e) {
            System.err.println(" Registration failed with unexpected error: " + e.getMessage());
            // Devolver 500 Internal Server Error para otros errores
            return ResponseEntity.status(500).body(null);
        }
    }

    // El endpoint de login ya devuelve AuthResponse, así que está bien
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        System.out.println(" Login attempt for email: " + request.getEmail());

        AuthResponse response = authService.login(request);
        System.out.println(" Login successful for user ID: " + response.getUserId() + " in institution ID: " + response.getInstitutionId());
        return ResponseEntity.ok(response);
    }
}