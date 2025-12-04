package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.UserDTO;
import com.grcontrol.grcontrol_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de usuarios
 * Endpoints para listar usuarios y filtrar por rol
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Obtener todos los usuarios del sistema
     * Solo accesible para ADMINISTRADOR y GERENTE
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<List<UserDTO.UserResponse>> getAllUsers() {
        List<UserDTO.UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/by-role/{role}
     * Obtener usuarios filtrados por rol
     * Solo accesible para ADMINISTRADOR y GERENTE
     */
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<List<UserDTO.UserResponse>> getUsersByRole(
        @PathVariable String role
    ) {
        // Normalizar el rol (agregar ROLE_ si no lo tiene)
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        List<UserDTO.UserResponse> users = userService.getUsersByRole(normalizedRole);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id}
     * Obtener un usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<UserDTO.UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserDTO.UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/users/active
     * Obtener solo usuarios activos
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<List<UserDTO.UserResponse>> getActiveUsers() {
        List<UserDTO.UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/users/{id}/deactivate
     * Desactivar un usuario (soft delete)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<UserDTO.OperationResponse> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(
                new UserDTO.OperationResponse(
                    true,
                    "Usuario desactivado exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new UserDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/users/{id}/activate
     * Reactivar un usuario
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<UserDTO.OperationResponse> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(
                new UserDTO.OperationResponse(
                    true,
                    "Usuario reactivado exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new UserDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/users/{id}
     * Actualizar datos de usuario (incluye cambio de contraseña)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<UserDTO.UserResponse> updateUser(
        @PathVariable Long id,
        @RequestBody UserDTO.UpdateUserRequest request
    ) {
        try {
            UserDTO.UserResponse response = userService.updateUser(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
