package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Pump usando Java Records
 */
public class PumpDTO {

    /**
     * Request para crear/actualizar surtidor
     */
    public record PumpRequest(
        Long islandId,
        String name,
        Integer position,
        String brand,
        String model,
        String serialNumber,
        LocalDate installationDate,
        Boolean isActive,
        String notes
    ) {}

    /**
     * Response básica de surtidor
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
        LocalDate installationDate,
        Boolean isActive,
        Integer nozzlesCount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response detallada con mangueras
     */
    public record PumpDetailResponse(
        Long id,
        Long islandId,
        String islandName,
        String name,
        Integer position,
        String brand,
        String model,
        String serialNumber,
        LocalDate installationDate,
        Boolean isActive,
        String notes,
        List<NozzleResponse> nozzles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response de manguera
     */
    public record NozzleResponse(
        Long id,
        String side, // LEFT, RIGHT
        Integer position,
        String fuelType,
        Double pricePerGallon,
        String color,
        Boolean isActive
    ) {}

    /**
     * Request para activar/desactivar surtidor
     */
    public record ToggleActiveRequest(
        Boolean isActive,
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
