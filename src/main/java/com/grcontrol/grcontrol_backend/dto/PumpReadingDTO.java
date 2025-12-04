package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para PumpReading endpoints (NUEVO FORMATO con nozzleId)
 */
public class PumpReadingDTO {

    /**
     * Request para crear lectura
     */
    public record CreateReadingRequest(
        Long sessionId,
        Long nozzleId,
        String readingType,      // ENTRY, EXIT
        String digits,           // JSON string de array
        Double difference,
        Boolean completed,
        Long baseReadingId       // Opcional: referencia a lectura anterior
    ) {}

    /**
     * Request para actualizar lectura
     */
    public record UpdateReadingRequest(
        String digits,
        Double difference,
        Boolean completed
    ) {}

    /**
     * Request para crear múltiples lecturas
     */
    public record BatchCreateRequest(
        Long sessionId,
        List<ReadingData> readings
    ) {}

    /**
     * Datos de lectura individual para batch
     */
    public record ReadingData(
        Long nozzleId,
        String readingType,
        String digits,
        Double difference,
        Boolean completed,
        Long baseReadingId
    ) {}

    /**
     * Response completo de lectura
     */
    public record ReadingResponse(
        Long id,
        Long sessionId,
        Long nozzleId,
        String nozzleFuelType,
        String nozzleSide,
        Integer nozzlePosition,
        String readingType,
        String digits,
        Double difference,
        Boolean completed,
        Long baseReadingId,
        LocalDateTime readingTimestamp,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response para lectura base (del turno anterior)
     */
    public record BaseReadingResponse(
        Long id,
        Long nozzleId,
        Long sessionId,
        String readingType,
        String digits,
        Double difference,
        LocalDateTime readingTimestamp,
        Boolean completed
    ) {}

    /**
     * Response para operaciones batch
     */
    public record BatchResponse(
        Boolean success,
        Integer totalCreated,
        Integer totalFailed,
        List<Long> createdIds,
        List<String> errors
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
