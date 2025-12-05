package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad NozzleReading - Lectura de contómetros de una manguera específica
 * Cada manguera tiene 3 tipos de lecturas: SOLES, GALLONS, CLOCK
 * Cada lectura tiene: valor inicial, valor final, diferencia
 */
@Entity
@Table(
    name = "nozzle_readings",
    indexes = {
        @Index(name = "idx_nozzle_session", columnList = "nozzle_id, session_id, reading_type"),
        @Index(name = "idx_session_timestamp", columnList = "session_id, reading_timestamp")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_nozzle_session_type", 
            columnNames = {"nozzle_id", "session_id", "reading_type"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"nozzle", "session", "modifiedBy"})
public class NozzleReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ShiftSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 10)
    private ReadingType readingType;

    // Valor inicial del contómetro (cuando inicia el turno)
    @Column(name = "initial_value", precision = 12, scale = 3)
    private BigDecimal initialValue;

    // Valor final del contómetro (cuando termina el turno)
    @Column(name = "final_value", precision = 12, scale = 3)
    private BigDecimal finalValue;

    // Diferencia calculada automáticamente (final - inicial)
    @Column(name = "difference", precision = 12, scale = 3)
    private BigDecimal difference;

    // Número de decimales configurados para esta lectura
    @Column(name = "decimals")
    private Integer decimals;

    // Timestamp de la lectura
    @Column(name = "reading_timestamp")
    private LocalDateTime readingTimestamp;

    // Indica si la lectura está completada (tiene valor final)
    @Column(nullable = false)
    private Boolean completed = false;

    // Indica si fue modificada por un administrador
    @Column(name = "was_modified")
    private Boolean wasModified = false;

    // Usuario que modificó (si aplica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_user_id")
    private User modifiedBy;

    // Timestamp de modificación
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // Razón de la modificación
    @Column(name = "modification_reason", length = 500)
    private String modificationReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (readingTimestamp == null) {
            readingTimestamp = LocalDateTime.now();
        }
        calculateDifference();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateDifference();
    }

    /**
     * Calcula automáticamente la diferencia entre final e inicial
     */
    public void calculateDifference() {
        if (finalValue != null && initialValue != null) {
            this.difference = finalValue.subtract(initialValue);
            this.completed = true;
        } else {
            this.difference = BigDecimal.ZERO;
            this.completed = false;
        }
    }

    /**
     * Establece el valor final y marca como completado
     */
    public void setFinalValueAndComplete(BigDecimal value) {
        this.finalValue = value;
        calculateDifference();
    }

    public enum ReadingType {
        SOLES,      // Lectura en dinero (S/.)
        GALLONS,    // Lectura en volumen (galones o litros)
        CLOCK       // Lectura de reloj (contador adicional)
    }
}
