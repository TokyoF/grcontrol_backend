package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.ShiftSessionDTO;
import com.grcontrol.grcontrol_backend.service.ShiftSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de sesiones de turno (NUEVO FORMATO)
 * Usa WorkerAssignment + Station + ShiftSchedule
 */
@RestController
@RequestMapping("/api/shift-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShiftSessionController {

    private final ShiftSessionService shiftSessionService;

    /**
     * POST /api/shift-sessions
     * Crear nueva sesión de turno con nuevo formato
     * Requiere: workerId, assignmentId, configurationSnapshot
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ShiftSessionDTO.SessionResponse> createSession(
        @RequestBody ShiftSessionDTO.CreateSessionRequest request
    ) {
        try {
            ShiftSessionDTO.SessionResponse response = shiftSessionService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/shift-sessions/{id}
     * Obtener información de una sesión por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ShiftSessionDTO.SessionResponse> getSessionById(
        @PathVariable Long id
    ) {
        try {
            ShiftSessionDTO.SessionResponse response = shiftSessionService.getSessionById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/shift-sessions/by-session-id/{sessionId}
     * Obtener información de una sesión por sessionId (generado por cliente)
     */
    @GetMapping("/by-session-id/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ShiftSessionDTO.SessionResponse> getSessionBySessionId(
        @PathVariable String sessionId
    ) {
        try {
            ShiftSessionDTO.SessionResponse response = shiftSessionService.getSessionBySessionId(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/shift-sessions/by-worker/{workerId}
     * Obtener sesiones de un trabajador
     */
    @GetMapping("/by-worker/{workerId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<ShiftSessionDTO.SessionSummary>> getSessionsByWorker(
        @PathVariable Long workerId
    ) {
        List<ShiftSessionDTO.SessionSummary> sessions = shiftSessionService.getSessionsByWorker(workerId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * GET /api/shift-sessions/active
     * Obtener todas las sesiones activas
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<ShiftSessionDTO.SessionSummary>> getActiveSessions() {
        List<ShiftSessionDTO.SessionSummary> sessions = shiftSessionService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * PUT /api/shift-sessions/{id}/close
     * Cerrar una sesión de turno
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ShiftSessionDTO.SessionResponse> closeSession(
        @PathVariable Long id
    ) {
        try {
            ShiftSessionDTO.SessionResponse response = shiftSessionService.closeSession(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/shift-sessions/{id}/pause
     * Pausar una sesión
     */
    @PutMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ShiftSessionDTO.OperationResponse> pauseSession(
        @PathVariable Long id
    ) {
        try {
            shiftSessionService.pauseSession(id);
            return ResponseEntity.ok(
                new ShiftSessionDTO.OperationResponse(
                    true,
                    "Sesión pausada exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftSessionDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/shift-sessions/{id}/resume
     * Reanudar una sesión pausada
     */
    @PutMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ShiftSessionDTO.OperationResponse> resumeSession(
        @PathVariable Long id
    ) {
        try {
            shiftSessionService.resumeSession(id);
            return ResponseEntity.ok(
                new ShiftSessionDTO.OperationResponse(
                    true,
                    "Sesión reanudada exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftSessionDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/shift-sessions/{id}/stats
     * Obtener estadísticas de una sesión
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ShiftSessionDTO.SessionStats> getSessionStats(
        @PathVariable Long id
    ) {
        try {
            ShiftSessionDTO.SessionStats stats = shiftSessionService.getSessionStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
