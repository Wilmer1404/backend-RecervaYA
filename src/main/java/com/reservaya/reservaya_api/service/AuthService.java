    package com.reservaya.reservaya_api.service;

    import com.reservaya.reservaya_api.dto.AuthResponse;
    import com.reservaya.reservaya_api.dto.LoginRequest;
    import com.reservaya.reservaya_api.model.User;
    import com.reservaya.reservaya_api.model.enums.Role;
    import com.reservaya.reservaya_api.repository.UserRepository;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    @Service
    public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.jwtService = jwtService;
            this.authenticationManager = authenticationManager;
        }

        public User register(User user) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new IllegalStateException("El correo electrónico ya está registrado.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(Role.USER);
            return userRepository.save(user);
        }

        // --- MÉTODO CORREGIDO ---
        public AuthResponse login(LoginRequest request) {
            // Dejamos que authenticationManager lance la excepción si las credenciales son incorrectas
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            // Si la autenticación es exitosa, continuamos
            var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado después de la autenticación"));
            
            var jwtToken = jwtService.generateToken(user);
            return AuthResponse.builder().token(jwtToken).build();
        }
    }
    
