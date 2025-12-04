package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;

/**
 * DTOs para ShiftSession endpoints (NUEVO FORMATO)
 */
public class ShiftSessionDTO {

    /**
     * Request para crear sesión de turno
     */
    public record CreateSessionRequest(
        Long workerId,
        Long assignmentId,
        String configurationSnapshot // JSON stringified
    ) {}

    /**
     * Response completo de sesión
     */
    public record SessionResponse(
        Long id,
        String sessionId,
        Long workerId,
        String workerName,
        Long stationId,
        String stationName,
        Long islandId,
        String islandName,
        Long shiftScheduleId,
        String shiftName,
        String shiftDisplayLabel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Double totalSales,
        Integer readingsCount,
        Integer movementsCount,
        Boolean hasArqueo,
        String configurationSnapshot,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response resumido de sesión (para listas)
     */
    public record SessionSummary(
        Long id,
        String sessionId,
        String workerName,
        String stationName,
        String islandName,
        String shiftDisplayLabel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Double totalSales
    ) {}

    /**
     * Estadísticas de sesión
     */
    public record SessionStats(
        Long sessionId,
        Double totalSales,
        Integer totalReadings,
        Integer completedReadings,
        Integer pendingReadings,
        Integer movementsCount,
        Double movementsTotal,
        Boolean hasArqueo,
        Double arqueoDifference,
        Integer durationMinutes
    ) {}

    /**
     * Response genérica de operación
     */
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}
}
