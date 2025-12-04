package com.grcontrol.grcontrol_backend.dto;

/**
 * Request para cambiar estado de una asignación
 */
public record ChangeStatusRequest(
    String status, // ACTIVE, CANCELLED, REPLACED
    String reason
) {}
