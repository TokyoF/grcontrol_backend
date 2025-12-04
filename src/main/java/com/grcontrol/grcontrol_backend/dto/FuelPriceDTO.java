package com.grcontrol.grcontrol_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs para gestión de precios de combustibles
 */
public class FuelPriceDTO {

    /**
     * Respuesta con información de precio actual
     */
    public record CurrentPriceResponse(
        Long id,
        String fuelType,
        BigDecimal pricePerGallon,
        LocalDateTime effectiveFrom,
        Long stationId,
        String stationName
    ) {}

    /**
     * Respuesta con información completa de precio (incluye historial)
     */
    public record PriceHistoryResponse(
        Long id,
        String fuelType,
        BigDecimal pricePerGallon,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveUntil,
        String changedByUsername,
        String notes,
        boolean isCurrent
    ) {}

    /**
     * Request para actualizar precio
     */
    public record UpdatePriceRequest(
        Long stationId,
        String fuelType,
        BigDecimal newPrice,
        String notes
    ) {}

    /**
     * Request para establecer precio inicial
     */
    public record SetInitialPriceRequest(
        Long stationId,
        String fuelType,
        BigDecimal price,
        LocalDateTime effectiveFrom,
        String notes
    ) {}

    /**
     * Respuesta de operación
     */
    public record OperationResponse(
        boolean success,
        String message,
        Long id,
        CurrentPriceResponse newPrice
    ) {}

    /**
     * Respuesta con todos los precios actuales de una estación
     */
    public record StationCurrentPricesResponse(
        Long stationId,
        String stationName,
        java.util.List<CurrentPriceResponse> prices,
        LocalDateTime retrievedAt
    ) {}

    /**
     * Request para obtener precio en una fecha específica
     */
    public record PriceAtDateRequest(
        Long stationId,
        String fuelType,
        LocalDateTime dateTime
    ) {}
}
