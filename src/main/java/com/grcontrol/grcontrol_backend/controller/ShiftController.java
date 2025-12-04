package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.ShiftDTO;
import com.grcontrol.grcontrol_backend.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de turnos
 * Endpoints diseñados para aplicación móvil con sincronización offline
 */
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permitir CORS para React Native
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * POST /api/shifts
     * Crear nueva sesión de turno
     */
    @PostMapping
    public ResponseEntity<ShiftDTO.SessionResponse> createSession(
        @RequestBody ShiftDTO.SessionRequest request
    ) {
        try {
            var response = shiftService.createSession(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/shifts/{sessionId}
     * Obtener información de una sesión
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ShiftDTO.SessionResponse> getSession(
        @PathVariable String sessionId
    ) {
        try {
            var response = shiftService.getSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/shifts/{sessionId}/close
     * Cerrar una sesión de turno
     */
    @PostMapping("/{sessionId}/close")
    public ResponseEntity<ShiftDTO.SessionResponse> closeSession(
        @PathVariable String sessionId,
        @RequestBody ShiftDTO.CloseSessionRequest request
    ) {
        try {
            var response = shiftService.closeSession(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/shifts/sync
     * Sincronización batch desde móvil
     * Acepta lecturas, movimientos y arqueo de una sola vez
     */
    @PostMapping("/sync")
    public ResponseEntity<ShiftDTO.OperationResponse> syncBatch(
        @RequestBody ShiftDTO.SyncBatchRequest request
    ) {
        var response = shiftService.syncBatch(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/shifts/{sessionId}/stats
     * Obtener estadísticas de una sesión
     */
    @GetMapping("/{sessionId}/stats")
    public ResponseEntity<ShiftDTO.StatsResponse> getStats(
        @PathVariable String sessionId
    ) {
        try {
            var response = shiftService.getSessionStats(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/shifts/health
     * Health check del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Shift service is running");
    }
}
