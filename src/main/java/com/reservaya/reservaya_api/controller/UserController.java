package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.UserDTO;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // Importar
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<UserDTO> getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
         UserDTO userDto = UserDTO.builder()
                 .id(currentUser.getId())
                 .name(currentUser.getName())
                 .email(currentUser.getEmail())
                 .role(currentUser.getRole())
                 .build();
        return ResponseEntity.ok(userDto);
    }

}