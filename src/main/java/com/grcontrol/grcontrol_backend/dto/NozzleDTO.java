package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Nozzle usando Java Records
 */
public class NozzleDTO {

    /**
     * Request para crear/actualizar manguera
     * El precio se obtiene automáticamente desde FuelPriceHistory según fuelType
     */
    public record NozzleRequest(
        Long pumpId,
        String side, // LEFT, RIGHT
        Integer position,
        String fuelType, // REGULAR, PREMIUM, DIESEL, GLP
        String fuelName, // "Regular 90", "Premium 95", etc. (opcional)
        String color,
        Boolean isActive
    ) {}

    /**
     * Response básica de manguera
     * El precio se obtiene dinámicamente desde FuelPriceHistory
     */
    public record NozzleResponse(
        Long id,
        Long pumpId,
        String pumpName,
        String side,
        Integer position,
        String fuelType,
        String fuelName,
        Double pricePerGallon, // Obtenido dinámicamente de FuelPriceHistory
        String color,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response detallada con información de surtidor e isla
     * El precio se obtiene dinámicamente desde FuelPriceHistory
     */
    public record NozzleDetailResponse(
        Long id,
        Long pumpId,
        String pumpName,
        Long islandId,
        String islandName,
        Long stationId,
        String stationName,
        String side,
        Integer position,
        String fuelType,
        String fuelName,
        Double pricePerGallon, // Obtenido dinámicamente de FuelPriceHistory
        String color,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Request para cambiar precio
     * DEPRECATED: Los precios ahora se manejan en FuelPriceController
     * Use /api/fuel-prices/update en su lugar
     */
    @Deprecated
    public record UpdatePriceRequest(
        Double newPrice,
        String reason,
        Long changedById
    ) {}

    /**
     * Request para activar/desactivar manguera
     */
    public record ToggleActiveRequest(
        Boolean isActive,
        String reason,
        Long changedById
    ) {}

    /**
     * Request para cambio de tipo de combustible
     */
    public record ChangeFuelTypeRequest(
        String newFuelType,
        String reason,
        Long changedById
    ) {}

    /**
     * Request batch para actualizar múltiples precios
     * DEPRECATED: Los precios ahora se manejan por tipo de combustible en FuelPriceController
     */
    @Deprecated
    public record BatchUpdatePricesRequest(
        List<PriceUpdate> updates,
        String reason,
        Long changedById
    ) {}

    /**
     * Actualización de precio individual
     * DEPRECATED
     */
    @Deprecated
    public record PriceUpdate(
        Long nozzleId,
        Double newPrice
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
     * Response de operación batch
     */
    public record BatchOperationResponse(
        Boolean success,
        String message,
        Integer successCount,
        Integer failureCount,
        List<String> errors
    ) {}
}
