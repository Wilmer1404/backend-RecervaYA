package com.reservaya.reservaya_api.model;

import com.reservaya.reservaya_api.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") // Usamos "users" porque "user" es una palabra reservada en PostgreSQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Patrón de diseño útil para construir objetos
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // La columna no puede ser nula
    private String name;

    @Column(unique = true, nullable = false) // El email debe ser único y no nulo
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Guarda el rol como texto ("USER", "ADMIN") en la BD
    private Role role;

    // --- Métodos de la interfaz UserDetails de Spring Security ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Usaremos el email como nombre de usuario para la autenticación
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}