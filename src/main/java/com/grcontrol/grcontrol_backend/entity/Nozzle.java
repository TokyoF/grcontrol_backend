package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Nozzle - Representa una manguera de combustible en un surtidor
 * Cada manguera tiene un tipo de combustible, lado (izquierdo/derecho)
 * El precio se obtiene dinámicamente desde FuelPriceHistory según el tipo de combustible
 */
@Entity
@Table(name = "nozzles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "pump")
public class Nozzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    // DEPRECATED: El precio ahora se maneja en FuelPriceHistory
    // Mantener el campo para compatibilidad con datos legacy
    @Column(name = "price_per_gallon", precision = 10, scale = 3)
    @Deprecated
    private BigDecimal pricePerGallon;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NozzleStatus status = NozzleStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean active = true;

    // ==================== CONFIGURACIÓN DE CONTADORES ====================
    
    // Tipo de medida principal (SOLES, GALLONS, LITROS, RELOJ)
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", length = 20)
    private ReadingType readingType = ReadingType.SOLES;
    
    // Configuración para SOLES
    @Column(name = "soles_total_digits")
    private Integer solesTotalDigits = 6; // Total de dígitos (ej: 123456 = 1234.56)
    
    @Column(name = "soles_decimals")
    private Integer solesDecimals = 2; // Cantidad de decimales
    
    // Configuración para GALONES
    @Column(name = "gallons_total_digits")
    private Integer gallonsTotalDigits = 6; // Total de dígitos
    
    @Column(name = "gallons_decimals")
    private Integer gallonsDecimals = 3; // Cantidad de decimales
    
    // Configuración para LITROS
    @Column(name = "liters_total_digits")
    private Integer litersTotalDigits = 6;
    
    @Column(name = "liters_decimals")
    private Integer litersDecimals = 2;
    
    // Configuración para RELOJ (contador mecánico)
    @Column(name = "clock_total_digits")
    private Integer clockTotalDigits = 8; // Total de dígitos para reloj
    
    @Column(name = "clock_decimals")
    private Integer clockDecimals = 0; // Por defecto 0 decimales para reloj (entero)
    
    // Si la manguera tiene múltiples contadores habilitados
    @Column(name = "has_soles_counter")
    private Boolean hasSolesCounter = true;
    
    @Column(name = "has_gallons_counter")
    private Boolean hasGallonsCounter = true;
    
    @Column(name = "has_liters_counter")
    private Boolean hasLitersCounter = false;
    
    @Column(name = "has_clock_counter")
    private Boolean hasClockCounter = false;

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
    
    public enum ReadingType {
        SOLES,     // Lectura en soles (dinero)
        GALLONS,   // Lectura en galones
        LITROS,    // Lectura en litros
        RELOJ      // Lectura de reloj/contador mecánico
    }
}
