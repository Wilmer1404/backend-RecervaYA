package com.reservaya.reservaya_api.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      // ESTA LÍNEA ES LA SOLUCIÓN: Le dice a Spring Security que use nuestra configuración de CORS.
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeHttpRequests(auth ->
        auth
          // Permitir peticiones OPTIONS para CORS preflight
          .requestMatchers("OPTIONS", "/**").permitAll()
          // Las rutas de autenticación (/register, /login) son públicas
          .requestMatchers("/api/v1/auth/**")
          .permitAll()
          // Cualquier otra petición requiere que el usuario esté autenticado
          .anyRequest()
          .authenticated()
      )
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authenticationProvider(authenticationProvider)
      .addFilterBefore(
        jwtAuthFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }

  // ESTE BEAN DEFINE Y EXPONE LAS REGLAS DE CORS PARA QUE SPRING SECURITY LAS USE
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Permitimos el origen de nuestro frontend y otros orígenes comunes para desarrollo
    configuration.setAllowedOrigins(List.of(
      "http://localhost:3000", 
      "http://127.0.0.1:3000",
      "http://0.0.0.0:3000"
    ));
    // Permitimos los métodos HTTP que el frontend usará
    configuration.setAllowedMethods(
      List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
    );
    // Permitimos todos los encabezados
    configuration.setAllowedHeaders(List.of("*"));
    // Permitimos que el navegador envíe credenciales (necesario para la autorización)
    configuration.setAllowCredentials(true);
    
    // Exponemos el header Authorization para que el frontend pueda leerlo
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Aplicamos esta configuración a TODAS las rutas
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

