package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.AuthResponse;
import com.reservaya.reservaya_api.dto.LoginRequest;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        System.out.println("📝 Registration attempt for email: " + user.getEmail());
        User registeredUser = authService.register(user);
        System.out.println("✅ User registered successfully with ID: " + registeredUser.getId());
        return registeredUser;
    }

    @PostMapping("/login")
    public AuthResponse loginUser(@RequestBody LoginRequest request) {
        System.out.println("🔐 Login attempt for email: " + request.getEmail());
        AuthResponse response = authService.login(request);
        System.out.println("✅ Login successful, token generated");
        return response;
    }
}