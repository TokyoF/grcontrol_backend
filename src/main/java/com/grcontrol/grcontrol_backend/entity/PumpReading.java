package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad PumpReading - Lectura de contómetros de combustible
 * Almacena entrada/salida y diferencia calculada
 */
@Entity
@Table(
    name = "pump_readings",
    indexes = {
        @Index(
            name = "idx_session_pump",
            columnList = "session_id,pump_id,side,nozzle_index,reading_type"
        ),
        @Index(
            name = "idx_session_nozzle",
            columnList = "session_id,nozzle_id,reading_type"
        ),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"session", "nozzle", "baseReading"})
public class PumpReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ShiftSession session;

    // NUEVO: Relación con Nozzle (reemplaza identificadores denormalizados)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nozzle_id")
    private Nozzle nozzle;

    // DEPRECATED: Mantener para compatibilidad con datos existentes
    @Column(name = "island_id")
    private Integer islandId;

    @Column(name = "island_name", length = 50)
    private String islandName;

    @Column(name = "pump_id")
    private Integer pumpId;

    @Column(name = "pump_name", length = 50)
    private String pumpName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PumpSide side;

    @Column(name = "nozzle_index")
    private Integer nozzleIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column(name = "fuel_name", length = 50)
    private String fuelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 10)
    private ReadingType readingType;

    @Column(name = "entry_digits", nullable = false, length = 50)
    private String entryDigits; // JSON array como string: ["1","2","3",...]

    @Column(name = "exit_digits", nullable = false, length = 50)
    private String exitDigits;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,3)")
    private Double difference;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "reading_timestamp", nullable = false)
    private LocalDateTime readingTimestamp;

    // NUEVO: Referencia a lectura base del turno anterior
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_reading_id")
    private PumpReading baseReading;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 20)
    private SyncStatus syncStatus = SyncStatus.SYNCED;

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
        RIGHT,
    }

    public enum FuelType {
        REGULAR,
        PREMIUM,
        DIESEL,
        GLP,
    }

    public enum ReadingType {
        SOLES,
        GALLONS,
    }

    public enum SyncStatus {
        PENDING,
        SYNCED,
        ERROR,
    }


}
