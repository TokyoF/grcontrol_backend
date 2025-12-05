package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.NozzleReadingDTO;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.service.NozzleReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de lecturas de contómetros
 * Endpoints para GRIFERO (registrar lecturas) y ADMIN/GERENTE (modificar)
 */
@RestController
@RequestMapping("/api/nozzle-readings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NozzleReadingController {

    private final NozzleReadingService nozzleReadingService;

    // ==================== CREATE OPERATIONS ====================

    /**
     * POST /api/nozzle-readings/nozzle
     * Crear todas las lecturas de una manguera (SOLES, GALLONS, CLOCK)
     * Usado por GRIFERO al iniciar turno
     */
    @PostMapping("/nozzle")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> createNozzleReadings(
            @RequestBody NozzleReadingDTO.CreateNozzleReadingsRequest request
    ) {
        try {
            var response = nozzleReadingService.createNozzleReadings(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * POST /api/nozzle-readings/session/initialize
     * Inicializar lecturas para toda una sesión (múltiples mangueras)
     * Usado por GRIFERO al comenzar el turno
     */
    @PostMapping("/session/initialize")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> initializeSessionReadings(
            @RequestBody NozzleReadingDTO.InitializeSessionReadingsRequest request
    ) {
        try {
            var response = nozzleReadingService.initializeSessionReadings(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * PUT /api/nozzle-readings/{id}/final-value
     * Actualizar valor final de una lectura
     * Usado por GRIFERO al terminar turno
     */
    @PutMapping("/{id}/final-value")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> updateFinalValue(
            @PathVariable Long id,
            @RequestBody NozzleReadingDTO.UpdateFinalValueRequest request
    ) {
        try {
            var response = nozzleReadingService.updateFinalValue(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/nozzle-readings/{id}/modify
     * Modificar lectura (solo ADMIN/GERENTE) con auditoría
     * Requiere razón de modificación
     */
    @PutMapping("/{id}/modify")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> modifyReading(
            @PathVariable Long id,
            @RequestBody NozzleReadingDTO.ModifyReadingRequest request,
            Authentication authentication
    ) {
        try {
            // Obtener ID del usuario autenticado
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            var response = nozzleReadingService.modifyReading(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== READ OPERATIONS ====================

    /**
     * GET /api/nozzle-readings/{id}
     * Obtener lectura específica por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getReading(@PathVariable Long id) {
        try {
            var response = nozzleReadingService.getReading(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/nozzle-readings/nozzle/{nozzleId}/session/{sessionId}
     * Obtener todas las lecturas de una manguera en una sesión
     */
    @GetMapping("/nozzle/{nozzleId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getNozzleReadingsBySession(
            @PathVariable Long nozzleId,
            @PathVariable Long sessionId
    ) {
        try {
            var response = nozzleReadingService.getNozzleReadingsBySession(nozzleId, sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzle-readings/session/{sessionId}
     * Obtener todas las lecturas de una sesión agrupadas por manguera
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getAllReadingsBySession(@PathVariable Long sessionId) {
        try {
            var response = nozzleReadingService.getAllReadingsBySession(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzle-readings/session/{sessionId}/summary
     * Obtener resumen completo de lecturas de una sesión
     * Incluye estadísticas y todas las lecturas agrupadas
     */
    @GetMapping("/session/{sessionId}/summary")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getSessionReadingsSummary(@PathVariable Long sessionId) {
        try {
            var response = nozzleReadingService.getSessionReadingsSummary(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzle-readings/session/{sessionId}/incomplete
     * Obtener lecturas incompletas de una sesión
     */
    @GetMapping("/session/{sessionId}/incomplete")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> getIncompleteReadings(@PathVariable Long sessionId) {
        try {
            var response = nozzleReadingService.getIncompleteReadings(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzle-readings/session/{sessionId}/modified
     * Obtener lecturas modificadas de una sesión (auditoría)
     */
    @GetMapping("/session/{sessionId}/modified")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> getModifiedReadings(@PathVariable Long sessionId) {
        try {
            var response = nozzleReadingService.getModifiedReadings(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * DELETE /api/nozzle-readings/{id}
     * Eliminar lectura (solo si no está completada)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> deleteReading(@PathVariable Long id) {
        try {
            nozzleReadingService.deleteReading(id);
            return ResponseEntity.ok(
                new NozzleReadingDTO.OperationResponse(
                    true,
                    "Lectura eliminada exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * DELETE /api/nozzle-readings/session/{sessionId}
     * Eliminar todas las lecturas de una sesión (uso administrativo)
     */
    @DeleteMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> deleteSessionReadings(@PathVariable Long sessionId) {
        try {
            var response = nozzleReadingService.deleteSessionReadings(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
