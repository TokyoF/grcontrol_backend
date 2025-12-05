package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;

/**
 * DTOs para Movement endpoints
 */
public class MovementDTO {

    /**
     * Request para crear movimiento
     */
    public record CreateMovementRequest(
        Long sessionId,
        String paymentMethod,    // EFECTIVO, TARJETA_CREDITO, etc.
        Double amount,
        String description,
        // VISA-specific fields (optional, only for TARJETA_CREDITO/DEBITO)
        String visaWorkerName,
        String vehicleType,      // AUTO, MOTO
        String vehicleBrand,
        String vehiclePlate,
        String vehicleColor
    ) {}

    /**
     * Request para actualizar movimiento
     */
    public record UpdateMovementRequest(
        String paymentMethod,
        Double amount,
        String description,
        // VISA-specific fields
        String visaWorkerName,
        String vehicleType,
        String vehicleBrand,
        String vehiclePlate,
        String vehicleColor
    ) {}

    /**
     * Response completo de movimiento
     */
    public record MovementResponse(
        Long id,
        String movementId,       // ID generado por cliente
        Long sessionId,
        String paymentMethod,
        Double amount,
        String description,
        // VISA fields
        String visaWorkerName,
        String vehicleType,
        String vehicleBrand,
        String vehiclePlate,
        String vehicleColor,
        LocalDateTime movementTimestamp,
        String syncStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
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
