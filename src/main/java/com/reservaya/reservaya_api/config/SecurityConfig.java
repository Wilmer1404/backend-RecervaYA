package com.reservaya.reservaya_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF (No es necesario para APIs REST con JWT)
            .csrf(csrf -> csrf.disable())
            // 2. Habilitar CORS explícitamente dentro de Seguridad
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 3. Configurar Rutas Públicas vs Privadas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                // IMPORTANTE: Permitir /auth/** y también /api/auth/** por si acaso
                .requestMatchers("/auth/**", "/api/auth/**").permitAll() 
                // Permitir acceso a recursos estáticos o documentación si tienes
                .requestMatchers("/error").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            // 4. Gestión de Sesión (Stateless porque usamos JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 5. Proveedor de autenticación y filtro JWT
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Definición explícita de CORS para Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permitir el origen de tu Frontend Angular
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); 
        // Permitir todos los métodos HTTP (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Permitir todas las cabeceras (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        // Permitir enviar credenciales (cookies/tokens) si fuera necesario
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}