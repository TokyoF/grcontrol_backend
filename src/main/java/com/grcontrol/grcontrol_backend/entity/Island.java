package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Island - Representa una isla de surtidores en una estación
 * Cada isla pertenece a una estación y contiene múltiples surtidores
 */
@Entity
@Table(
    name = "islands",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"station_id", "name"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Island {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false, length = 50)
    private String name; // "Isla 1", "Isla Norte", etc.

    @Column(length = 500)
    private String description; // Descripción de la isla

    @Column
    private Integer position; // Orden visual en la estación

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private IslandStatus status = IslandStatus.ACTIVE;

    @OneToMany(mappedBy = "island", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Pump> pumps = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addPump(Pump pump) {
        pumps.add(pump);
        pump.setIsland(this);
    }

    public void removePump(Pump pump) {
        pumps.remove(pump);
        pump.setIsland(null);
    }

    public enum IslandStatus {
        ACTIVE,
        OFFLINE,
        MAINTENANCE
    }
}
