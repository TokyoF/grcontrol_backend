package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.WorkerAssignmentDTO;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.service.WorkerAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de asignaciones de trabajadores
 * Endpoints para ADMINISTRADOR y GERENTE
 */
@RestController
@RequestMapping("/api/worker-assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkerAssignmentController {

    private final WorkerAssignmentService workerAssignmentService;

    // ==================== CREATE OPERATIONS ====================

    /**
     * POST /api/worker-assignments
     * Crear asignación individual
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> createAssignment(
            @RequestBody WorkerAssignmentDTO.AssignmentRequest request,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            var response = workerAssignmentService.createAssignment(request, user.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * POST /api/worker-assignments/weekly
     * Crear asignaciones semanales completas (7 días)
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/weekly")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> createWeeklyAssignments(
            @RequestBody WorkerAssignmentDTO.WeeklyAssignmentRequest request,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            var response = workerAssignmentService.createWeeklyAssignments(request, user.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.BatchOperationResponse(
                    false,
                    e.getMessage(),
                    0,
                    List.of(),
                    List.of(e.getMessage())
                )
            );
        }
    }

    // ==================== READ OPERATIONS ====================

    /**
     * GET /api/worker-assignments/{id}
     * Obtener asignación por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR', 'GRIFERO')")
    public ResponseEntity<?> getAssignment(@PathVariable Long id) {
        try {
            var response = workerAssignmentService.getAssignment(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/worker-assignments/worker/{workerId}/week/{weekStartDate}
     * Obtener asignaciones semanales de un trabajador
     */
    @GetMapping("/worker/{workerId}/week/{weekStartDate}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR', 'GRIFERO')")
    public ResponseEntity<?> getWorkerWeeklyAssignments(
            @PathVariable Long workerId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        try {
            var response = workerAssignmentService.getWorkerWeeklyAssignments(workerId, weekStartDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/worker-assignments/island/{islandId}/week/{weekStartDate}
     * Obtener horario semanal de una isla
     */
    @GetMapping("/island/{islandId}/week/{weekStartDate}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getIslandWeeklySchedule(
            @PathVariable Long islandId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        try {
            var response = workerAssignmentService.getIslandWeeklySchedule(islandId, weekStartDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/worker-assignments/station/{stationId}/week/{weekStartDate}
     * Obtener horario semanal completo de una estación (todas las islas)
     */
    @GetMapping("/station/{stationId}/week/{weekStartDate}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getStationWeeklySchedule(
            @PathVariable Long stationId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        try {
            var response = workerAssignmentService.getStationWeeklySchedule(stationId, weekStartDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/worker-assignments/active
     * Obtener asignación activa de un trabajador para una fecha específica
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> getActiveAssignmentForDate(
            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            var response = workerAssignmentService.getActiveAssignmentForDate(workerId, date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * PUT /api/worker-assignments/{id}/replace-worker
     * Reemplazar trabajador en una asignación
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}/replace-worker")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> replaceWorker(
            @PathVariable Long id,
            @RequestBody WorkerAssignmentDTO.ReplaceWorkerRequest request
    ) {
        try {
            var response = workerAssignmentService.replaceWorker(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/worker-assignments/{id}/cancel
     * Cancelar asignación
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> cancelAssignment(
            @PathVariable Long id,
            @RequestBody String reason
    ) {
        try {
            var response = workerAssignmentService.cancelAssignment(id, reason);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * DELETE /api/worker-assignments/{id}
     * Eliminar asignación
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            var response = workerAssignmentService.deleteAssignment(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new WorkerAssignmentDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
