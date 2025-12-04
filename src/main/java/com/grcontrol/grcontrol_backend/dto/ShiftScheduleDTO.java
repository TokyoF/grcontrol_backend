package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DTOs para ShiftSchedule usando Java Records
 */
public class ShiftScheduleDTO {

    /**
     * Request para crear/actualizar horario de turno
     */
    public record ShiftScheduleRequest(
        Long stationId,
        String name,
        String displayLabel, // e.g., "6-14", "14-22", "22-6"
        LocalTime startTime,
        LocalTime endTime,
        Boolean isOvernight,
        Boolean isActive
    ) {}

    /**
     * Response básica de horario
     */
    public record ShiftScheduleResponse(
        Long id,
        Long stationId,
        String stationName,
        String name,
        String displayLabel,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isOvernight,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response con estadísticas de uso
     */
    public record ShiftScheduleDetailResponse(
        Long id,
        Long stationId,
        String stationName,
        String name,
        String displayLabel,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isOvernight,
        Boolean isActive,
        Integer activeAssignmentsCount,
        Integer totalSessionsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Resumen simple de horario
     */
    public record ShiftScheduleSummary(
        Long id,
        String name,
        String displayLabel,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isOvernight
    ) {}

    /**
     * Request para activar/desactivar horario
     */
    public record ToggleActiveRequest(
        Boolean isActive,
        String reason
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
     * Response de lista de horarios por estación
     */
    public record StationSchedulesResponse(
        Long stationId,
        String stationName,
        List<ShiftScheduleResponse> schedules,
        Integer activeCount,
        Integer inactiveCount
    ) {}
}
