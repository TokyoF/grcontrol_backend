package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.MovementDTO;
import com.grcontrol.grcontrol_backend.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de movimientos de caja
 */
@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovementController {

    private final MovementService movementService;

    /**
     * POST /api/movements
     * Crear nuevo movimiento
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<MovementDTO.MovementResponse> createMovement(
        @RequestBody MovementDTO.CreateMovementRequest request
    ) {
        try {
            MovementDTO.MovementResponse response = movementService.createMovement(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/movements/{id}
     * Obtener movimiento por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<MovementDTO.MovementResponse> getMovementById(
        @PathVariable Long id
    ) {
        try {
            MovementDTO.MovementResponse response = movementService.getMovementById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/movements/by-session/{sessionId}
     * Obtener movimientos de una sesión
     */
    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<MovementDTO.MovementResponse>> getMovementsBySession(
        @PathVariable Long sessionId
    ) {
        List<MovementDTO.MovementResponse> movements = movementService.getMovementsBySession(sessionId);
        return ResponseEntity.ok(movements);
    }

    /**
     * PUT /api/movements/{id}
     * Actualizar movimiento
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<MovementDTO.MovementResponse> updateMovement(
        @PathVariable Long id,
        @RequestBody MovementDTO.UpdateMovementRequest request
    ) {
        try {
            MovementDTO.MovementResponse response = movementService.updateMovement(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/movements/{id}
     * Eliminar movimiento
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<MovementDTO.OperationResponse> deleteMovement(
        @PathVariable Long id
    ) {
        try {
            movementService.deleteMovement(id);
            return ResponseEntity.ok(
                new MovementDTO.OperationResponse(
                    true,
                    "Movimiento eliminado exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new MovementDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
