package com.grcontrol.grcontrol_backend.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para WorkerAssignment usando Java Records
 */
public class WorkerAssignmentDTO {

    /**
     * Request para crear asignación individual
     */
    public record AssignmentRequest(
        Long workerId,
        Long islandId,
        Long shiftScheduleId,
        LocalDate weekStartDate,
        DayOfWeek dayOfWeek,
        Boolean isRestDay,
        String notes
    ) {}

    /**
     * Request para asignación semanal completa
     */
    public record WeeklyAssignmentRequest(
        Long workerId,
        LocalDate weekStartDate,
        List<DayAssignment> assignments
    ) {}

    /**
     * Asignación de un día específico
     */
    public record DayAssignment(
        DayOfWeek dayOfWeek,
        Long islandId,
        Long shiftScheduleId,
        Boolean isRestDay
    ) {}

    /**
     * Response básica de asignación
     */
    public record AssignmentResponse(
        Long id,
        Long workerId,
        String workerName,
        Long islandId,
        String islandName,
        Long shiftScheduleId,
        String shiftScheduleName,
        String shiftDisplayLabel,
        LocalDate weekStartDate,
        DayOfWeek dayOfWeek,
        Boolean isRestDay,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response de asignaciones semanales de un trabajador
     */
    public record WeeklyAssignmentResponse(
        Long workerId,
        String workerName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        List<AssignmentResponse> assignments,
        Integer restDaysCount,
        Integer workDaysCount
    ) {}

    /**
     * Response de asignaciones de una isla para una semana
     */
    public record IslandWeeklyScheduleResponse(
        Long islandId,
        String islandName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        List<DaySchedule> schedule
    ) {}

    /**
     * Horario de un día para una isla
     */
    public record DaySchedule(
        DayOfWeek dayOfWeek,
        List<ShiftAssignment> shifts
    ) {}

    /**
     * Asignación de turno específico
     */
    public record ShiftAssignment(
        Long assignmentId,
        Long shiftScheduleId,
        String shiftName,
        String shiftDisplayLabel,
        Long workerId,
        String workerName,
        String workerFirstName,
        String workerLastName,
        Boolean isRestDay,
        String status
    ) {}

    /**
     * Request para reemplazar trabajador
     */
    public record ReplaceWorkerRequest(
        Long newWorkerId,
        String reason
    ) {}

    /**
     * Response de asignación activa para una fecha
     */
    public record ActiveAssignmentResponse(
        Long assignmentId,  // Cambiar 'id' a 'assignmentId' para mobile
        Long workerId,
        String workerName,
        Long islandId,
        String islandName,
        Long stationId,
        String stationName,
        Long shiftScheduleId,
        String shiftName,
        String shiftDisplayLabel,
        String startTime,  // Hora de inicio (HH:mm:ss)
        String endTime,    // Hora de fin (HH:mm:ss)
        Boolean isOvernight,  // Si el turno cruza medianoche
        LocalDate date,
        DayOfWeek dayOfWeek,
        LocalDate weekStartDate,  // Lunes de la semana
        Boolean isRestDay
    ) {}

    /**
     * Request para buscar asignación activa
     */
    public record FindActiveAssignmentRequest(
        Long workerId,
        LocalDate date
    ) {}

    /**
     * Response de operación
     */
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}

    /**
     * Response de operación batch (semana completa)
     */
    public record BatchOperationResponse(
        Boolean success,
        String message,
        Integer assignmentsCreated,
        List<Long> assignmentIds,
        List<String> errors
    ) {}

    /**
     * Estadísticas de asignaciones de un trabajador
     */
    public record WorkerAssignmentStatsResponse(
        Long workerId,
        String workerName,
        Integer totalAssignments,
        Integer activeAssignments,
        Integer restDaysThisMonth,
        Integer workDaysThisMonth,
        List<String> assignedIslands
    ) {}
}
