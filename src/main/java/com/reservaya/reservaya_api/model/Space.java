package com.reservaya.reservaya_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // Le dice a JPA que esta clase es una tabla en la base de datos
@Table(name = "spaces") // Opcional: especifica el nombre de la tabla
@Data // Lombok: genera automáticamente getters, setters, toString, etc.
@NoArgsConstructor // Lombok: genera un constructor sin argumentos, requerido por JPA
public class Space {

    @Id // Marca este campo como la clave primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Le dice a Postgres que genere el ID automáticamente
    private Long id;

    private String name;
    private String type; // Ej: "sports", "study", "lab"
    private int capacity;
    private String image; // Podría ser un emoji o una URL a una imagen
}