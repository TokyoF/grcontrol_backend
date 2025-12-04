package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Nozzle usando Java Records
 */
public class NozzleDTO {

    /**
     * Request para crear/actualizar manguera
     */
    public record NozzleRequest(
        Long pumpId,
        String side, // LEFT, RIGHT
        Integer position,
        String fuelType, // REGULAR, PREMIUM, DIESEL, GLP
        Double pricePerGallon,
        String color,
        Boolean isActive
    ) {}

    /**
     * Response básica de manguera
     */
    public record NozzleResponse(
        Long id,
        Long pumpId,
        String pumpName,
        String side,
        Integer position,
        String fuelType,
        Double pricePerGallon,
        String color,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response detallada con información de surtidor e isla
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
        Double pricePerGallon,
        String color,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Request para cambiar precio
     */
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
     */
    public record BatchUpdatePricesRequest(
        List<PriceUpdate> updates,
        String reason,
        Long changedById
    ) {}

    /**
     * Actualización de precio individual
     */
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
