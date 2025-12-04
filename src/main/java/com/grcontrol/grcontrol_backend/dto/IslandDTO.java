package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Island usando Java Records
 */
public class IslandDTO {

    /**
     * Request para crear/actualizar isla
     */
    public record IslandRequest(
        Long stationId,
        String name,
        String description,
        String status, // ACTIVE, OFFLINE, MAINTENANCE
        Integer position
    ) {}

    /**
     * Response básica de isla
     */
    public record IslandResponse(
        Long id,
        Long stationId,
        String stationName,
        String name,
        String description,
        String status,
        Integer position,
        Integer pumpsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response detallada con surtidores y mangueras
     */
    public record IslandDetailResponse(
        Long id,
        Long stationId,
        String stationName,
        String name,
        String description,
        String status,
        Integer position,
        List<PumpWithNozzles> pumps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Surtidor con sus mangueras
     */
    public record PumpWithNozzles(
        Long id,
        String name,
        Integer position,
        String brand,
        String model,
        Boolean isActive,
        List<NozzleSummary> nozzles
    ) {}

    /**
     * Resumen de manguera
     * El precio se obtiene dinámicamente desde FuelPriceHistory
     */
    public record NozzleSummary(
        Long id,
        String side, // LEFT, RIGHT
        Integer position,
        String fuelType,
        String fuelName,
        Double pricePerGallon, // Obtenido dinámicamente
        String color,
        Boolean isActive
    ) {}

    /**
     * Request para cambiar estado de isla
     */
    public record ChangeStatusRequest(
        String status, // ACTIVE, OFFLINE, MAINTENANCE
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
}
