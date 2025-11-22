package com.reservaya.reservaya_api.service;

import com.reservaya.reservaya_api.dto.AuthResponse;
import com.reservaya.reservaya_api.dto.LoginRequest;
import com.reservaya.reservaya_api.dto.RegisterRequest; // Importar nuevo DTO
import com.reservaya.reservaya_api.model.Institution; // Importar Institution
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.model.enums.Role;
import com.reservaya.reservaya_api.repository.InstitutionRepository; // Importar repo
import com.reservaya.reservaya_api.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Usar @RequiredArgsConstructor
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException; // Importar para manejo de errores
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar para transacciones

import java.util.HashMap; // Para claims del JWT
import java.util.Map;     // Para claims del JWT

@Service
@RequiredArgsConstructor // Lombok genera el constructor con los campos final
public class AuthService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository; // Inyectar repo
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional // Asegura que ambas creaciones (Institution y User) ocurran o ninguna
    public User register(RegisterRequest request) {
        // 1. Validar si la institución ya existe
        institutionRepository.findByName(request.getInstitutionName()).ifPresent(i -> {
            throw new IllegalStateException("Ya existe una institución con el nombre: " + request.getInstitutionName());
        });
        // 1.1 (Opcional) Validar si el dominio ya está registrado (si aplica)
         if (request.getInstitutionEmailDomain() != null && !request.getInstitutionEmailDomain().isEmpty()) {
             institutionRepository.findByEmailDomain(request.getInstitutionEmailDomain()).ifPresent(i -> {
                 throw new IllegalStateException("El dominio de correo ya está registrado por otra institución: " + request.getInstitutionEmailDomain());
             });
         }

        // 2. Validar si el email del admin ya existe
        userRepository.findByEmail(request.getAdminEmail()).ifPresent(u -> {
            throw new IllegalStateException("El correo electrónico del administrador ya está registrado.");
        });

        // 3. Crear la Institución
        Institution newInstitution = Institution.builder()
                .name(request.getInstitutionName())
                .type(request.getInstitutionType())
                .emailDomain(request.getInstitutionEmailDomain()) // Guardar dominio
                .build();
        Institution savedInstitution = institutionRepository.save(newInstitution); // Guardar primero

        // 4. Crear el Usuario Administrador y asociarlo a la institución
        User adminUser = User.builder()
                .name(request.getAdminName())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .role(Role.ADMIN)
                .institution(savedInstitution)
                .build();

        return userRepository.save(adminUser); // Guardar usuario
    }

    public AuthResponse login(LoginRequest request) {
        // Autenticar (puede lanzar BadCredentialsException si falla)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw e;
        }


        // Si la autenticación es exitosa, obtener el usuario (ahora incluye la institución)
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado después de la autenticación exitosa")); // Error inesperado si ocurre

        // Crear claims adicionales para el JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("institutionId", user.getInstitution().getId());
        extraClaims.put("role", user.getRole().name()); // Añadir rol como string
        extraClaims.put("userId", user.getId()); // Añadir userId

        // Generar el token JWT con los claims extra
        var jwtToken = jwtService.generateToken(extraClaims, user);

        // Devolver respuesta completa
        return AuthResponse.builder()
                .token(jwtToken)
                .userRole(user.getRole())
                .institutionId(user.getInstitution().getId())
                .userId(user.getId())
                .userName(user.getName())
                .build();
    }
}