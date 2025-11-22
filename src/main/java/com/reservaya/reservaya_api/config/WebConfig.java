package com.reservaya.reservaya_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; 
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) { 
        registry.addMapping("/api/**") 
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://0.0.0.0:3000"
                )
                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); 
    }
}