package com.grcontrol.grcontrol_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para User endpoints
 */
public class UserDTO {

    /**
     * Response completo de usuario (sin password)
     */
    public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        Boolean isActive,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    /**
     * Response simplificado de usuario (para listas)
     */
    public record UserSummary(
        Long id,
        String username,
        String firstName,
        String lastName,
        List<String> roles,
        Boolean isActive
    ) {}

    /**
     * Response genérica de operación
     */
    public record OperationResponse(
        Boolean success,
        String message,
        Long id
    ) {}

    /**
     * Request para actualizar usuario (admin puede cambiar todo)
     */
    public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String password  // Opcional, solo si se quiere cambiar
    ) {}
}
