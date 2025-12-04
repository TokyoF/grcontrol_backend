package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;

/**
 * DTOs para Arqueo endpoints
 */
public class ArqueoDTO {

    /**
     * Request para crear arqueo
     */
    public record CreateArqueoRequest(
        Long sessionId,
        Double efectivo,
        Double tarjetaCredito,
        Double tarjetaDebito,
        Double valeInterno,
        Double deposito,
        Double totalCash,
        Double totalSales,
        Double difference,
        String status,           // OPEN, BALANCED, UNBALANCED
        String notes
    ) {}

    /**
     * Request para actualizar arqueo
     */
    public record UpdateArqueoRequest(
        Double efectivo,
        Double tarjetaCredito,
        Double tarjetaDebito,
        Double valeInterno,
        Double deposito,
        Double totalCash,
        Double totalSales,
        Double difference,
        String status,
        String notes
    ) {}

    /**
     * Response completo de arqueo
     */
    public record ArqueoResponse(
        Long id,
        String arqueoId,         // ID generado por cliente
        Long sessionId,
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
        LocalDateTime arqueoTimestamp,
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
