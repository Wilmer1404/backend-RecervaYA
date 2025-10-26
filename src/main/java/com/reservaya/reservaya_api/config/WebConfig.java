package com.reservaya.reservaya_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // Importar anotación de Spring
// o import lombok.NonNull; si prefieres usar la de Lombok
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) { // Añadir @NonNull aquí
        registry.addMapping("/api/**") // Aplica CORS a todas las rutas bajo /api/
                // Orígenes permitidos (tu frontend y localhost para desarrollo)
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://0.0.0.0:3000" // Añadir si accedes desde otra máquina en la red local
                        // Agrega aquí la URL de tu frontend cuando esté desplegado
                )
                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                // Cabeceras permitidas (usar "*" es común en desarrollo, pero sé específico en producción si es posible)
                .allowedHeaders("*")
                // Permitir envío de credenciales (cookies, encabezados de autorización)
                .allowCredentials(true)
                 // Tiempo que el navegador puede cachear la respuesta preflight (OPTIONS)
                .maxAge(3600); // 1 hora
    }
}