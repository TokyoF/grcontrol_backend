package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Nozzle - Representa una manguera de combustible en un surtidor
 * Cada manguera tiene un tipo de combustible, lado (izquierdo/derecho), y precio
 */
@Entity
@Table(name = "nozzles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nozzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pump_id", nullable = false)
    private Pump pump;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PumpSide side; // LEFT, RIGHT

    @Column
    private Integer position; // Posición en el lado del surtidor

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType; // REGULAR, PREMIUM, DIESEL, GLP

    @Column(name = "fuel_name", length = 50)
    private String fuelName; // "Regular 90", "Premium 95", "Diesel B5", etc.

    @Column(length = 50)
    private String color; // Color de la manguera (Rojo, Verde, Azul, etc.)

    @Column(name = "price_per_gallon", precision = 10, scale = 3)
    private BigDecimal pricePerGallon; // Precio actual por galón

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NozzleStatus status = NozzleStatus.ACTIVE;

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

    public enum PumpSide {
        LEFT,
        RIGHT
    }

    public enum FuelType {
        REGULAR,
        PREMIUM,
        DIESEL,
        GLP
    }

    public enum NozzleStatus {
        ACTIVE,
        OFFLINE
    }
}
