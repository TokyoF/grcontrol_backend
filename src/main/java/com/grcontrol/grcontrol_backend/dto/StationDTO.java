package com.grcontrol.grcontrol_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para Station usando Java Records
 */
public class StationDTO {

    /**
     * Request para crear/actualizar estación
     */
    public record StationRequest(
        String name,
        String address,
        String phone,
        Boolean isActive
    ) {}

    /**
     * Response básica de estación
     */
    public record StationResponse(
        Long id,
        String name,
        String address,
        String phone,
        Boolean isActive,
        Integer islandsCount,
        Integer administratorsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response detallada de estación con islas
     */
    public record StationDetailResponse(
        Long id,
        String name,
        String address,
        String phone,
        Boolean isActive,
        List<IslandSummary> islands,
        List<AdministratorSummary> administrators,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Resumen de isla para respuestas anidadas
     */
    public record IslandSummary(
        Long id,
        String name,
        String status,
        Integer pumpsCount,
        Integer position
    ) {}

    /**
     * Resumen de administrador
     */
    public record AdministratorSummary(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
    ) {}

    /**
     * Request para asignar administradores a estación
     */
    public record AssignAdministratorsRequest(
        List<Long> administratorIds
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
     * Response de horario de turno
     */
    public record ShiftScheduleResponse(
        Long id,
        String displayLabel,
        String name,
        String startTime,
        String endTime,
        Boolean isActive
    ) {}

    /**
     * Configuración completa de manguera
     */
    public record NozzleFullConfig(
        Long id,
        String name,
        String fuelType,
        String side,
        Integer sideIndex,
        BigDecimal currentPrice,
        Boolean isActive
    ) {}

    /**
     * Configuración completa de surtidor
     */
    public record PumpFullConfig(
        Long id,
        String name,
        Boolean isActive,
        List<NozzleFullConfig> nozzles
    ) {}

    /**
     * Configuración completa de isla
     */
    public record IslandFullConfig(
        Long id,
        String name,
        String status,
        Integer position,
        List<PumpFullConfig> pumps
    ) {}

    /**
     * Configuración completa de estación (para móvil)
     */
    public record StationFullConfigResponse(
        Long id,
        String name,
        String address,
        String phone,
        Boolean isActive,
        List<IslandFullConfig> islands
    ) {}
}
