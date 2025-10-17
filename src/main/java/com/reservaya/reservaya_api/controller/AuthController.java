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
        return authService.register(user);
    }

    @PostMapping("/login")
    public AuthResponse loginUser(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}