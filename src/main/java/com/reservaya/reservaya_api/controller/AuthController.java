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
        try {
            User registeredUser = authService.register(request);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}