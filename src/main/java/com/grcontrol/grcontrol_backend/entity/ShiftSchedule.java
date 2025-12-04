package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad ShiftSchedule - Representa un horario de turno para una estación
 * Define los horarios de trabajo (ej: 6-14, 14-22, 22-6)
 */
@Entity
@Table(name = "shift_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false, length = 100)
    private String name; // "Turno Mañana", "Turno Tarde", "Turno Noche"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 06:00

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 14:00

    @Column(name = "display_label", length = 20)
    private String displayLabel; // "6-14", "14-22", "22-6"

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

        // Auto-generar displayLabel si no está definido
        if (displayLabel == null && startTime != null && endTime != null) {
            displayLabel = startTime.getHour() + "-" + endTime.getHour();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
