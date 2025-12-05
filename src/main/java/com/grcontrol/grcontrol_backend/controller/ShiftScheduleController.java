package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.ShiftScheduleDTO;
import com.grcontrol.grcontrol_backend.service.ShiftScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de horarios de turno
 * Endpoints para ADMINISTRADOR y GERENTE
 */
@RestController
@RequestMapping("/api/shift-schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShiftScheduleController {

    private final ShiftScheduleService shiftScheduleService;

    // ==================== CREATE OPERATIONS ====================

    /**
     * POST /api/shift-schedules
     * Crear nuevo horario de turno
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> createShiftSchedule(
            @RequestBody ShiftScheduleDTO.ShiftScheduleRequest request
    ) {
        try {
            var response = shiftScheduleService.createShiftSchedule(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== READ OPERATIONS ====================

    /**
     * GET /api/shift-schedules/{id}
     * Obtener horario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getShiftSchedule(@PathVariable Long id) {
        try {
            var response = shiftScheduleService.getShiftSchedule(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/shift-schedules/station/{stationId}
     * Obtener todos los horarios de una estación
     */
    @GetMapping("/station/{stationId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<?> getStationSchedules(@PathVariable Long stationId) {
        try {
            var response = shiftScheduleService.getStationSchedules(stationId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/shift-schedules/station/{stationId}/active
     * Obtener horarios activos de una estación
     */
    @GetMapping("/station/{stationId}/active")
    public ResponseEntity<?> getActiveSchedules(@PathVariable Long stationId) {
        try {
            var response = shiftScheduleService.getActiveSchedules(stationId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/shift-schedules
     * Obtener todos los horarios de todas las estaciones
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<List<ShiftScheduleDTO.ShiftScheduleResponse>> getAllSchedules() {
        var response = shiftScheduleService.getAllSchedules();
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * PUT /api/shift-schedules/{id}
     * Actualizar horario de turno
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> updateShiftSchedule(
            @PathVariable Long id,
            @RequestBody ShiftScheduleDTO.ShiftScheduleRequest request
    ) {
        try {
            var response = shiftScheduleService.updateShiftSchedule(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * PUT /api/shift-schedules/{id}/toggle-active
     * Activar/desactivar horario
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> toggleActive(
            @PathVariable Long id,
            @RequestBody ShiftScheduleDTO.ToggleActiveRequest request
    ) {
        try {
            var response = shiftScheduleService.toggleActive(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * DELETE /api/shift-schedules/{id}
     * Eliminar horario (solo si no tiene asignaciones)
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> deleteShiftSchedule(@PathVariable Long id) {
        try {
            var response = shiftScheduleService.deleteShiftSchedule(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ShiftScheduleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
