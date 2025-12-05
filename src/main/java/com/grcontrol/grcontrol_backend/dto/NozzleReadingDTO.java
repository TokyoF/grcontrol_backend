package com.grcontrol.grcontrol_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para gestión de lecturas de contómetros
 */
public class NozzleReadingDTO {

    /**
     * Request para crear/actualizar una lectura individual
     */
    public record ReadingRequest(
        Long nozzleId,
        Long sessionId,
        String readingType,      // SOLES, GALLONS, CLOCK
        BigDecimal initialValue,
        BigDecimal finalValue,
        Integer decimals
    ) {}

    /**
     * Request para crear lecturas de una manguera (todas a la vez)
     */
    public record CreateNozzleReadingsRequest(
        Long nozzleId,
        Long sessionId,
        ReadingValues soles,
        ReadingValues gallons,
        ReadingValues clock
    ) {}

    /**
     * Valores de una lectura específica
     */
    public record ReadingValues(
        BigDecimal initialValue,
        BigDecimal finalValue,
        Integer decimals
    ) {}

    /**
     * Response con información de una lectura
     */
    public record ReadingResponse(
        Long id,
        Long nozzleId,
        String nozzleFuelType,
        String nozzleSide,
        Long sessionId,
        String readingType,
        BigDecimal initialValue,
        BigDecimal finalValue,
        BigDecimal difference,
        Integer decimals,
        Boolean completed,
        Boolean wasModified,
        String modifiedByUsername,
        LocalDateTime modifiedAt,
        String modificationReason,
        LocalDateTime readingTimestamp,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response agrupado por manguera (todas las lecturas de una manguera)
     */
    public record NozzleReadingsResponse(
        Long nozzleId,
        String fuelType,
        String side,
        Integer position,
        String color,
        Long pumpId,
        String pumpName,
        ReadingDetail solesReading,
        ReadingDetail gallonsReading,
        ReadingDetail clockReading
    ) {}

    /**
     * Detalle de una lectura específica
     */
    public record ReadingDetail(
        Long id,
        BigDecimal initialValue,
        BigDecimal finalValue,
        BigDecimal difference,
        Integer decimals,
        Boolean completed,
        Boolean wasModified
    ) {}

    /**
     * Request para actualizar valor final
     */
    public record UpdateFinalValueRequest(
        BigDecimal finalValue
    ) {}

    /**
     * Request para modificar lectura (solo admin)
     */
    public record ModifyReadingRequest(
        BigDecimal initialValue,
        BigDecimal finalValue,
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
     * Response con resumen de sesión
     */
    public record SessionReadingsSummary(
        Long sessionId,
        String sessionOperator,
        LocalDateTime sessionStart,
        Integer totalNozzles,
        Integer completedReadings,
        Integer incompleteReadings,
        Integer modifiedReadings,
        List<NozzleReadingsResponse> nozzleReadings
    ) {}

    /**
     * Request para inicializar lecturas de una sesión
     */
    public record InitializeSessionReadingsRequest(
        Long sessionId,
        List<NozzleInitialReading> nozzles
    ) {}

    /**
     * Lectura inicial de una manguera
     */
    public record NozzleInitialReading(
        Long nozzleId,
        BigDecimal solesInitial,
        BigDecimal gallonsInitial,
        BigDecimal clockInitial
    ) {}
}
