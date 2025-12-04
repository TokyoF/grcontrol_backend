package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad FuelPriceHistory - Historial de precios de combustibles
 * Permite rastrear cambios de precio y aplicar el precio correcto según la fecha
 */
@Entity
@Table(
    name = "fuel_price_history",
    indexes = {
        @Index(name = "idx_fuel_price_station_type_date", 
               columnList = "station_id, fuel_type, effective_from DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"station", "changedBy"})
public class FuelPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    @Column(name = "price_per_gallon", nullable = false, precision = 10, scale = 3)
    private BigDecimal pricePerGallon;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (effectiveFrom == null) {
            effectiveFrom = LocalDateTime.now();
        }
    }

    /**
     * Verifica si este precio está activo en una fecha específica
     */
    public boolean isActiveAt(LocalDateTime dateTime) {
        boolean afterStart = !dateTime.isBefore(effectiveFrom);
        boolean beforeEnd = effectiveUntil == null || dateTime.isBefore(effectiveUntil);
        return afterStart && beforeEnd;
    }

    /**
     * Verifica si este es el precio actual (sin fecha de fin)
     */
    public boolean isCurrent() {
        return effectiveUntil == null;
    }

    public enum FuelType {
        REGULAR,
        PREMIUM,
        DIESEL,
        GLP
    }
}
