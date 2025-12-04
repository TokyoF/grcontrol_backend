package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.PumpConfigurationHistoryDTO;
import com.grcontrol.grcontrol_backend.entity.PumpConfigurationHistory;
import com.grcontrol.grcontrol_backend.service.ConfigurationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para consulta de historial de cambios de configuración
 * Proporciona endpoints de auditoría y reportes
 */
@RestController
@RequestMapping("/api/configuration-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfigurationHistoryController {

    private final ConfigurationHistoryService historyService;

    /**
     * GET /api/configuration-history/{id}
     * Obtener cambio de configuración por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpConfigurationHistoryDTO.ConfigChangeResponse> getConfigChange(
        @PathVariable Long id
    ) {
        try {
            var response = historyService.getConfigChange(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/configuration-history/nozzle/{nozzleId}
     * Obtener historial completo de una manguera
     */
    @GetMapping("/nozzle/{nozzleId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpConfigurationHistoryDTO.NozzleHistoryResponse> getNozzleHistory(
        @PathVariable Long nozzleId
    ) {
        try {
            var response = historyService.getNozzleHistory(nozzleId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/configuration-history/date-range
     * Obtener cambios en un rango de fechas con estadísticas
     *
     * Query params:
     * - startDate: ISO DateTime (e.g., 2025-12-01T00:00:00)
     * - endDate: ISO DateTime (e.g., 2025-12-31T23:59:59)
     *
     * Ejemplo: GET /api/configuration-history/date-range?startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpConfigurationHistoryDTO.DateRangeHistoryResponse> getHistoryByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        var response = historyService.getHistoryByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/configuration-history/session/{sessionId}
     * Obtener cambios que afectaron una sesión específica
     * Útil para ver qué configuraciones cambiaron durante un turno activo
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpConfigurationHistoryDTO.SessionAffectedChangesResponse> getSessionAffectedChanges(
        @PathVariable Long sessionId
    ) {
        var response = historyService.getSessionAffectedChanges(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/configuration-history/nozzle/{nozzleId}/type/{changeType}
     * Obtener cambios de un tipo específico para una manguera
     *
     * changeType: PRICE_CHANGE, ACTIVATION, DEACTIVATION, FUEL_TYPE_CHANGE
     * Ejemplo: GET /api/configuration-history/nozzle/5/type/PRICE_CHANGE
     */
    @GetMapping("/nozzle/{nozzleId}/type/{changeType}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<PumpConfigurationHistoryDTO.ConfigChangeResponse>> getChangesByType(
        @PathVariable Long nozzleId,
        @PathVariable String changeType
    ) {
        try {
            PumpConfigurationHistory.ConfigChangeType type =
                PumpConfigurationHistory.ConfigChangeType.valueOf(changeType);
            var response = historyService.getChangesByType(nozzleId, type);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/configuration-history/user/{userId}
     * Obtener cambios realizados por un usuario
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<List<PumpConfigurationHistoryDTO.ConfigChangeResponse>> getChangesByUser(
        @PathVariable Long userId
    ) {
        var response = historyService.getChangesByUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/configuration-history/pump/{pumpId}
     * Obtener todos los cambios de configuración de un surtidor
     */
    @GetMapping("/pump/{pumpId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<PumpConfigurationHistoryDTO.ConfigChangeResponse>> getChangesByPump(
        @PathVariable Long pumpId
    ) {
        var response = historyService.getChangesByPump(pumpId);
        return ResponseEntity.ok(response);
    }

    // ==================== REPORTING ====================

    /**
     * GET /api/configuration-history/audit-report
     * Generar reporte de auditoría para un rango de fechas
     *
     * Query params:
     * - startDate: ISO DateTime
     * - endDate: ISO DateTime
     * - generatedBy: Nombre del usuario que genera el reporte
     *
     * Ejemplo: GET /api/configuration-history/audit-report?startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59&generatedBy=Admin
     *
     * Requiere rol GERENTE o FACTURADOR
     */
    @GetMapping("/audit-report")
    @PreAuthorize("hasAnyRole('GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpConfigurationHistoryDTO.AuditReportResponse> generateAuditReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam String generatedBy
    ) {
        var response = historyService.generateAuditReport(startDate, endDate, generatedBy);
        return ResponseEntity.ok(response);
    }
}
