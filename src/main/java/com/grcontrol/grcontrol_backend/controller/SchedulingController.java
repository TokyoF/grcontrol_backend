package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.ChangeStatusRequest;
import com.grcontrol.grcontrol_backend.dto.WorkerAssignmentDTO;
import com.grcontrol.grcontrol_backend.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de asignaciones de trabajadores y horarios
 */
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchedulingController {

    private final SchedulingService schedulingService;

    // ==================== ASSIGNMENT CREATION ====================

    /**
     * POST /api/scheduling/assignments
     * Crear asignación individual
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<WorkerAssignmentDTO.AssignmentResponse> createAssignment(
        @RequestBody WorkerAssignmentDTO.AssignmentRequest request
    ) {
        try {
            var response = schedulingService.createAssignment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/scheduling/assignments/weekly
     * Crear asignaciones para toda una semana
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/assignments/weekly")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<WorkerAssignmentDTO.BatchOperationResponse> createWeeklyAssignment(
        @RequestBody WorkerAssignmentDTO.WeeklyAssignmentRequest request
    ) {
        var response = schedulingService.createWeeklyAssignment(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * GET /api/scheduling/assignments/{id}
     * Obtener asignación por ID
     */
    @GetMapping("/assignments/{id}")
    public ResponseEntity<WorkerAssignmentDTO.AssignmentResponse> getAssignment(
        @PathVariable Long id
    ) {
        try {
            var response = schedulingService.getAssignment(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/scheduling/active-assignment
     * Buscar asignación activa de un trabajador para una fecha específica
     * Endpoint crítico para app móvil: determina qué turno le corresponde al trabajador hoy
     *
     * Query params: workerId, date (YYYY-MM-DD)
     * Ejemplo: GET /api/scheduling/active-assignment?workerId=5&date=2025-12-01
     */
    @GetMapping("/active-assignment")
    public ResponseEntity<WorkerAssignmentDTO.ActiveAssignmentResponse> findActiveAssignment(
        @RequestParam Long workerId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var request = new WorkerAssignmentDTO.FindActiveAssignmentRequest(workerId, date);
        var response = schedulingService.findActiveAssignment(request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/scheduling/workers/{workerId}/week
     * Obtener asignaciones semanales de un trabajador
     *
     * Query param: weekStartDate (YYYY-MM-DD, debe ser lunes)
     * Ejemplo: GET /api/scheduling/workers/5/week?weekStartDate=2025-12-01
     */
    @GetMapping("/workers/{workerId}/week")
    public ResponseEntity<WorkerAssignmentDTO.WeeklyAssignmentResponse> getWorkerWeeklyAssignments(
        @PathVariable Long workerId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        try {
            var response = schedulingService.getWorkerWeeklyAssignments(workerId, weekStartDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/scheduling/islands/{islandId}/week
     * Obtener horario semanal completo de una isla
     * Muestra qué trabajador está asignado a cada turno de cada día
     *
     * Query param: weekStartDate (YYYY-MM-DD, debe ser lunes)
     * Ejemplo: GET /api/scheduling/islands/3/week?weekStartDate=2025-12-01
     */
    @GetMapping("/islands/{islandId}/week")
    public ResponseEntity<WorkerAssignmentDTO.IslandWeeklyScheduleResponse> getIslandWeeklySchedule(
        @PathVariable Long islandId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        try {
            var response = schedulingService.getIslandWeeklySchedule(islandId, weekStartDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/scheduling/workers/{workerId}/active-assignments
     * Obtener todas las asignaciones activas de un trabajador
     */
    @GetMapping("/workers/{workerId}/active-assignments")
    public ResponseEntity<List<WorkerAssignmentDTO.AssignmentResponse>> getActiveAssignmentsByWorker(
        @PathVariable Long workerId
    ) {
        var response = schedulingService.getActiveAssignmentsByWorker(workerId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/scheduling/workers/{workerId}/stats
     * Obtener estadísticas de asignaciones de un trabajador
     */
    @GetMapping("/workers/{workerId}/stats")
    public ResponseEntity<WorkerAssignmentDTO.WorkerAssignmentStatsResponse> getWorkerStats(
        @PathVariable Long workerId
    ) {
        try {
            var response = schedulingService.getWorkerStats(workerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * POST /api/scheduling/assignments/{id}/change-status
     * Cambiar estado de asignación (ACTIVE, CANCELLED, REPLACED)
     * Requiere rol ADMINISTRADOR
     */
    @PostMapping("/assignments/{id}/change-status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<WorkerAssignmentDTO.OperationResponse> changeStatus(
        @PathVariable Long id,
        @RequestBody ChangeStatusRequest request
    ) {
        try {
            var response = schedulingService.changeStatus(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/scheduling/assignments/{id}/replace-worker
     * Reemplazar trabajador en una asignación
     * Marca la asignación actual como REPLACED y crea una nueva con el nuevo trabajador
     * Requiere rol ADMINISTRADOR
     */
    @PostMapping("/assignments/{id}/replace-worker")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<WorkerAssignmentDTO.OperationResponse> replaceWorker(
        @PathVariable Long id,
        @RequestBody WorkerAssignmentDTO.ReplaceWorkerRequest request
    ) {
        try {
            var response = schedulingService.replaceWorker(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
