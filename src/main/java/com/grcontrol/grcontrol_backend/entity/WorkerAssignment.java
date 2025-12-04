package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad WorkerAssignment - Representa la asignación de un trabajador a una isla
 * Maneja horarios semanales con días de trabajo y descanso
 */
@Entity
@Table(
    name = "worker_assignments",
    indexes = {
        @Index(name = "idx_worker_week", columnList = "worker_id, week_start_date"),
        @Index(name = "idx_island_schedule", columnList = "island_id, shift_schedule_id, day_of_week")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker; // Usuario con rol GRIFERO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "island_id", nullable = false)
    private Island island;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_schedule_id", nullable = false)
    private ShiftSchedule shiftSchedule;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate; // Lunes de la semana

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, ..., SUNDAY

    @Column(name = "is_rest_day")
    private Boolean isRestDay = false; // Día de descanso

    public Boolean getRestDay() {
        return isRestDay;
    }

    public void setRestDay(Boolean restDay) {
        this.isRestDay = restDay;
    }

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy; // Admin que creó la asignación

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AssignmentStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        REPLACED
    }
}
