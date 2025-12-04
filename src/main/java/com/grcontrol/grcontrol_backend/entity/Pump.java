package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Pump - Representa un surtidor en una isla
 * Cada surtidor pertenece a una isla y contiene múltiples mangueras (nozzles)
 */
@Entity
@Table(name = "pumps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"island", "nozzles"})
public class Pump {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "island_id", nullable = false)
    private Island island;

    @Column(nullable = false, length = 50)
    private String name; // "Surtidor 1", "Bomba A", etc.

    @Column
    private Integer position; // Posición en la isla

    @Column(length = 100)
    private String brand; // Marca del surtidor

    @Column(length = 100)
    private String model; // Modelo del surtidor

    @Column(length = 100)
    private String serialNumber; // Número de serie

    @Column
    private java.time.LocalDate installationDate; // Fecha de instalación

    @Column(length = 1000)
    private String notes; // Notas adicionales

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PumpStatus status = PumpStatus.ACTIVE;

    @OneToMany(mappedBy = "pump", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Nozzle> nozzles = new HashSet<>();

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
    public void addNozzle(Nozzle nozzle) {
        nozzles.add(nozzle);
        nozzle.setPump(this);
    }

    public void removeNozzle(Nozzle nozzle) {
        nozzles.remove(nozzle);
        nozzle.setPump(null);
    }

    public enum PumpStatus {
        ACTIVE,
        OFFLINE,
        MAINTENANCE
    }
}
