package com.grcontrol.grcontrol_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para configuración de surtidores y mangueras
 */
public class PumpConfigDTO {

    /**
     * DTO para configuración completa de una manguera
     */
    public record NozzleConfigRequest(
        String fuelType,
        String fuelName,
        String color,
        String side,
        Integer position,
        
        // Configuración de contadores
        String readingType,           // SOLES, GALLONS, LITROS, RELOJ
        Integer solesTotalDigits,
        Integer solesDecimals,
        Integer gallonsTotalDigits,
        Integer gallonsDecimals,
        Integer litersTotalDigits,
        Integer litersDecimals,
        Integer clockTotalDigits,
        Integer clockDecimals,
        
        // Qué contadores están habilitados
        Boolean hasSolesCounter,
        Boolean hasGallonsCounter,
        Boolean hasLitersCounter,
        Boolean hasClockCounter,
        
        // Estado
        String status,
        Boolean active
    ) {}

    /**
     * DTO de respuesta con la configuración de una manguera
     */
    public record NozzleConfigResponse(
        Long id,
        Long pumpId,
        String fuelType,
        String fuelName,
        String color,
        String side,
        Integer position,
        BigDecimal currentPrice,      // Precio actual del combustible
        
        // Configuración de contadores
        String readingType,
        Integer solesTotalDigits,
        Integer solesDecimals,
        Integer gallonsTotalDigits,
        Integer gallonsDecimals,
        Integer litersTotalDigits,
        Integer litersDecimals,
        Integer clockTotalDigits,
        Integer clockDecimals,
        
        // Qué contadores están habilitados
        Boolean hasSolesCounter,
        Boolean hasGallonsCounter,
        Boolean hasLitersCounter,
        Boolean hasClockCounter,
        
        // Estado
        String status,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * DTO para crear/actualizar un surtidor
     */
    public record PumpRequest(
        Long islandId,
        String name,
        Integer position,
        String brand,
        String model,
        String serialNumber,
        String installationDate,
        String notes,
        String status,
        Boolean active
    ) {}

    /**
     * DTO de respuesta con información completa del surtidor
     */
    public record PumpResponse(
        Long id,
        Long islandId,
        String islandName,
        String name,
        Integer position,
        String brand,
        String model,
        String serialNumber,
        String installationDate,
        String notes,
        String status,
        Boolean active,
        List<NozzleConfigResponse> nozzles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * DTO para actualizar precio de combustible
     */
    public record FuelPriceUpdateRequest(
        String fuelType,
        BigDecimal pricePerGallon,
        String notes
    ) {}

    /**
     * DTO de respuesta con historial de precios
     */
    public record FuelPriceResponse(
        Long id,
        String fuelType,
        BigDecimal pricePerGallon,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveUntil,
        String changedByUsername,
        String notes,
        Boolean isCurrent
    ) {}

    /**
     * DTO para crear una isla
     */
    public record IslandRequest(
        String name,
        String description,
        Integer position,
        String status,
        Boolean active
    ) {}

    /**
     * DTO de respuesta con información de isla
     */
    public record IslandResponse(
        Long id,
        String stationName,
        String name,
        String description,
        Integer position,
        String status,
        Boolean active,
        List<PumpResponse> pumps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response genérico de operación
     */
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}
}
