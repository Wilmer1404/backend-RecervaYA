package com.reservaya.reservaya_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "spaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // Ej: "sports", "study", "lab"

    @Column(nullable = false)
    private int capacity;

    private String image; // Podría ser un emoji o una URL a una imagen

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Institution institution;

    // --- GETTER EXPLÍCITO ---
    public Long getId() {
        return id;
    }
}