package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Shift Session usando Java Records (inmutables y concisos)
 */
public class ShiftDTO {

    // Request para crear/actualizar sesión
    public record SessionRequest(
        String sessionId,
        String operatorId,
        String shiftTime,
        LocalDateTime startTime,
        Integer stationId,
        String stationName
    ) {}

    // Response de sesión completa
    public record SessionResponse(
        Long id,
        String sessionId,
        String operatorName,
        String shiftTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Double totalSales,
        Integer stationId,
        String stationName,
        Integer readingsCount,
        Integer movementsCount,
        LocalDateTime createdAt
    ) {}

    // Request para cerrar sesión
    public record CloseSessionRequest(
        String sessionId,
        LocalDateTime endTime
    ) {}

    // Sync batch request (para sincronización offline)
    public record SyncBatchRequest(
        String sessionId,
        List<ReadingRequest> readings,
        List<MovementRequest> movements,
        ArqueoRequest arqueo
    ) {}

    // Reading request
    public record ReadingRequest(
        String sessionId,
        Integer islandId,
        String islandName,
        Integer pumpId,
        String pumpName,
        String side,
        Integer nozzleIndex,
        String fuelType,
        String fuelName,
        String readingType,
        String entryDigits,
        String exitDigits,
        Double difference,
        Boolean completed,
        LocalDateTime readingTimestamp
    ) {}

    // Movement request
    public record MovementRequest(
        String movementId,
        String sessionId,
        String paymentMethod,
        Double amount,
        String description,
        LocalDateTime movementTimestamp,
        // VISA-specific fields
        String visaWorkerName,
        String vehicleType,
        String vehicleBrand,
        String vehiclePlate,
        String vehicleColor
    ) {}

    // Arqueo request
    public record ArqueoRequest(
        String arqueoId,
        String sessionId,
        Double efectivo,
        Double tarjetaCredito,
        Double tarjetaDebito,
        Double valeInterno,
        Double deposito,
        Double totalCash,
        Double totalSales,
        Double difference,
        String status,
        String notes,
        LocalDateTime arqueoTimestamp
    ) {}

    // Response genérica para operaciones
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}

    // Response para estadísticas
    public record StatsResponse(
        Double totalSales,
        SalesByFuel salesByFuel,
        Integer totalReadings,
        Integer completedReadings,
        Integer totalMovements,
        Double totalMovementsAmount
    ) {}

    public record SalesByFuel(
        Double regular,
        Double premium,
        Double diesel,
        Double glp
    ) {}

    // Reading update request
    public record ReadingUpdateRequest(
        String entryDigits,
        String exitDigits,
        Double difference
    ) {}

    // General stats response
    public record GeneralStatsResponse(
        Double totalSales,
        Integer totalSessions,
        Integer activeSessions,
        Integer completedSessions,
        Double averageSalesPerSession
    ) {}

    // Daily sales response
    public record DailySalesResponse(
        String date,
        Long stationId,
        String stationName,
        Double totalSales,
        Integer totalTransactions
    ) {}

    // Sales comparison response
    public record SalesComparisonResponse(
        Long stationId,
        String stationName,
        Double totalSales,
        Integer totalTransactions,
        Double averageSales
    ) {}
}
