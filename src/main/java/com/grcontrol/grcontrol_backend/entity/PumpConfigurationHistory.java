package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad PumpConfigurationHistory - Historial de cambios en configuración de mangueras
 * Registra todos los cambios realizados a las mangueras (estado, precio, tipo de combustible)
 */
@Entity
@Table(name = "pump_configuration_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"nozzle", "changedBy", "affectedSession"})
public class PumpConfigurationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private ConfigChangeType changeType;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_shift_session_id")
    private ShiftSession affectedSession; // Si el cambio afectó un turno activo

    @PrePersist
    protected void onCreate() {
        if (changeTimestamp == null) {
            changeTimestamp = LocalDateTime.now();
        }
    }

    public enum ConfigChangeType {
        STATUS_CHANGE,
        PRICE_UPDATE,
        PRICE_CHANGE,
        FUEL_TYPE_CHANGE,
        NOZZLE_ADDED,
        NOZZLE_REMOVED,
        ACTIVATION,
        DEACTIVATION
    }
}
