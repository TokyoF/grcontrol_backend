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
        String description
    ) {}

    /**
     * Request para actualizar movimiento
     */
    public record UpdateMovementRequest(
        String paymentMethod,
        Double amount,
        String description
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
