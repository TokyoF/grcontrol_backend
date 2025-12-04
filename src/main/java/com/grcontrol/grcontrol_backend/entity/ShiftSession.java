package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad Turno - Representa una sesión de trabajo de un operario
 * Alineado con tabla 'turno' del esquema de base de datos
 */
@Entity
@Table(name = "shift_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"operator", "station", "shiftSchedule", "assignment", "readings", "movements"})
public class ShiftSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId; // ID generado por el cliente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    // DEPRECATED: Mantener para compatibilidad con datos existentes
    @Column(name = "shift_time", length = 10)
    private String shiftTime; // "6-14", "14-22", "22-6"

    // NUEVO: Relación con Station (reemplaza stationId/stationName)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id_fk")
    private Station station;

    // NUEVO: Relación con ShiftSchedule (reemplaza shiftTime)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_schedule_id")
    private ShiftSchedule shiftSchedule;

    // NUEVO: Referencia a la asignación semanal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_assignment_id")
    private WorkerAssignment assignment;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "total_sales", columnDefinition = "DECIMAL(10,2)")
    private Double totalSales = 0.0;

    // DEPRECATED: Mantener para compatibilidad con datos existentes
    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "station_name", length = 100)
    private String stationName;

    // NUEVO: Snapshot de configuración al inicio del turno (configuración híbrida)
    @Column(name = "configuration_snapshot", columnDefinition = "TEXT")
    private String configurationSnapshot;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 20)
    private SyncStatus syncStatus = SyncStatus.SYNCED;

    @OneToMany(
        mappedBy = "session",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PumpReading> readings = new ArrayList<>();

    @OneToMany(
        mappedBy = "session",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Movement> movements = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastActivity == null) {
            lastActivity = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }

    public enum SessionStatus {
        ACTIVE,
        PAUSED,
        COMPLETED,
    }

    public enum SyncStatus {
        PENDING,
        SYNCED,
        ERROR,
    }
}
