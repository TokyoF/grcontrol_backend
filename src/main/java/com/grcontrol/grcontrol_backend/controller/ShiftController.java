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

    /**
     * GET /api/shifts
     * Obtener turnos con filtros
     */
    @GetMapping
    public ResponseEntity<?> getShifts(
        @RequestParam(required = false) Long stationId,
        @RequestParam(required = false) Long operatorId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            var shifts = shiftService.getShifts(stationId, operatorId, status, startDate, endDate);
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/shifts/by-id/{id}
     * Obtener un turno por su ID de base de datos
     */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<?> getShiftById(@PathVariable Long id) {
        try {
            var shift = shiftService.getShiftById(id);
            return ResponseEntity.ok(shift);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/shifts/readings/{id}
     * Actualizar una lectura
     */
    @PutMapping("/readings/{id}")
    public ResponseEntity<?> updateReading(
        @PathVariable Long id,
        @RequestBody ShiftDTO.ReadingUpdateRequest request
    ) {
        try {
            var reading = shiftService.updateReading(id, request);
            return ResponseEntity.ok(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/shifts/{id}/complete
     * Completar un turno
     */
    @PostMapping("/by-id/{id}/complete")
    public ResponseEntity<?> completeShift(@PathVariable Long id) {
        try {
            var shift = shiftService.completeShift(id);
            return ResponseEntity.ok(shift);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/shifts/by-station
     * Obtener turnos por estación
     */
    @GetMapping("/by-station")
    public ResponseEntity<?> getShiftsByStation(
        @RequestParam Long stationId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            var shifts = shiftService.getShiftsByStation(stationId, startDate, endDate);
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/shifts/stats
     * Obtener estadísticas generales
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getGeneralStats(
        @RequestParam(required = false) Long stationId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            var stats = shiftService.getGeneralStats(stationId, startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/shifts/daily-sales
     * Obtener ventas diarias por estación
     */
    @GetMapping("/daily-sales")
    public ResponseEntity<?> getDailySales(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        try {
            var dailySales = shiftService.getDailySales(startDate, endDate);
            return ResponseEntity.ok(dailySales);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/shifts/sales-comparison
     * Obtener comparativa de ventas por estación
     */
    @GetMapping("/sales-comparison")
    public ResponseEntity<?> getSalesComparison(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        try {
            var comparison = shiftService.getSalesComparison(startDate, endDate);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
