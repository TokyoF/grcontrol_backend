package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para PumpConfigurationHistory usando Java Records
 */
public class PumpConfigurationHistoryDTO {

    /**
     * Request para registrar cambio de configuración
     */
    public record ConfigChangeRequest(
        Long nozzleId,
        String changeType, // PRICE_CHANGE, ACTIVATION, DEACTIVATION, FUEL_TYPE_CHANGE
        String previousValue,
        String newValue,
        Long changedById,
        Long affectedSessionId,
        String reason,
        String notes
    ) {}

    /**
     * Response de cambio de configuración
     */
    public record ConfigChangeResponse(
        Long id,
        Long nozzleId,
        String nozzleSide,
        String fuelType,
        Long pumpId,
        String pumpName,
        Long islandId,
        String islandName,
        String changeType,
        String previousValue,
        String newValue,
        LocalDateTime changeTimestamp,
        Long changedById,
        String changedByName,
        Long affectedSessionId,
        String reason,
        String notes
    ) {}

    /**
     * Response de historial de una manguera
     */
    public record NozzleHistoryResponse(
        Long nozzleId,
        String nozzleSide,
        String fuelType,
        Long pumpId,
        String pumpName,
        List<ConfigChangeResponse> changes,
        Integer totalChanges,
        LocalDateTime firstChange,
        LocalDateTime lastChange
    ) {}

    /**
     * Response de cambios en un rango de fechas
     */
    public record DateRangeHistoryResponse(
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<ConfigChangeResponse> changes,
        Integer totalChanges,
        ChangeStatistics statistics
    ) {}

    /**
     * Estadísticas de cambios
     */
    public record ChangeStatistics(
        Integer totalChanges,
        Integer priceChanges,
        Integer activations,
        Integer deactivations,
        Integer fuelTypeChanges,
        List<TopChanger> topChangers
    ) {}

    /**
     * Usuario que más cambios realizó
     */
    public record TopChanger(
        Long userId,
        String userName,
        Integer changesCount
    ) {}

    /**
     * Response de cambios que afectaron una sesión
     */
    public record SessionAffectedChangesResponse(
        Long sessionId,
        String sessionIdentifier,
        List<ConfigChangeResponse> changes,
        Integer totalChanges,
        LocalDateTime sessionStartTime,
        LocalDateTime sessionEndTime
    ) {}

    /**
     * Request para buscar historial por filtros
     */
    public record HistoryFilterRequest(
        Long nozzleId,
        Long pumpId,
        Long islandId,
        Long stationId,
        String changeType,
        Long changedById,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer page,
        Integer size
    ) {}

    /**
     * Response paginada de historial
     */
    public record HistoryPageResponse(
        List<ConfigChangeResponse> changes,
        Integer currentPage,
        Integer totalPages,
        Long totalElements,
        Boolean hasNext,
        Boolean hasPrevious
    ) {}

    /**
     * Resumen de cambio para auditoría
     */
    public record AuditSummary(
        LocalDateTime changeTimestamp,
        String changeType,
        String location, // "Isla X - Surtidor Y - Manguera Z"
        String changedBy,
        String summary, // "Precio: 12.50 → 12.80"
        String reason
    ) {}

    /**
     * Response de auditoría para reporte
     */
    public record AuditReportResponse(
        LocalDateTime reportDate,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<AuditSummary> changes,
        Integer totalChanges,
        String generatedBy
    ) {}

    /**
     * Response de operación
     */
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}
}
