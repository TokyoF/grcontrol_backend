package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.StationDTO;
import com.grcontrol.grcontrol_backend.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de estaciones de servicio
 */
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StationController {

    private final StationService stationService;

    /**
     * POST /api/stations
     * Crear nueva estación
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<StationDTO.StationResponse> createStation(
        @RequestBody StationDTO.StationRequest request
    ) {
        try {
            var response = stationService.createStation(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/stations/{id}
     * Obtener estación por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StationDTO.StationResponse> getStation(
        @PathVariable Long id
    ) {
        try {
            var response = stationService.getStation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/stations/{id}/detail
     * Obtener estación con islas y administradores
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<StationDTO.StationDetailResponse> getStationDetail(
        @PathVariable Long id
    ) {
        try {
            var response = stationService.getStationDetail(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/stations
     * Obtener todas las estaciones
     */
    @GetMapping
    public ResponseEntity<List<StationDTO.StationResponse>> getAllStations() {
        var response = stationService.getAllStations();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/stations/active
     * Obtener solo estaciones activas
     */
    @GetMapping("/active")
    public ResponseEntity<List<StationDTO.StationResponse>> getActiveStations() {
        var response = stationService.getActiveStations();
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/stations/{id}
     * Actualizar estación
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<StationDTO.StationResponse> updateStation(
        @PathVariable Long id,
        @RequestBody StationDTO.StationRequest request
    ) {
        try {
            var response = stationService.updateStation(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/stations/{id}
     * Eliminar estación
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== ADMINISTRATOR MANAGEMENT ====================

    /**
     * POST /api/stations/{id}/administrators
     * Asignar administradores a una estación (reemplaza los existentes)
     * Requiere rol GERENTE
     */
    @PostMapping("/{id}/administrators")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<StationDTO.OperationResponse> assignAdministrators(
        @PathVariable Long id,
        @RequestBody StationDTO.AssignAdministratorsRequest request
    ) {
        try {
            var response = stationService.assignAdministrators(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/stations/{stationId}/administrators/{userId}
     * Agregar un administrador a la estación
     * Requiere rol GERENTE
     */
    @PostMapping("/{stationId}/administrators/{userId}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<StationDTO.OperationResponse> addAdministrator(
        @PathVariable Long stationId,
        @PathVariable Long userId
    ) {
        try {
            var response = stationService.addAdministrator(stationId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/stations/{stationId}/administrators/{userId}
     * Remover un administrador de la estación
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{stationId}/administrators/{userId}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<StationDTO.OperationResponse> removeAdministrator(
        @PathVariable Long stationId,
        @PathVariable Long userId
    ) {
        try {
            var response = stationService.removeAdministrator(stationId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/stations/by-administrator/{userId}
     * Obtener estaciones de un administrador
     */
    @GetMapping("/by-administrator/{userId}")
    public ResponseEntity<List<StationDTO.StationResponse>> getStationsByAdministrator(
        @PathVariable Long userId
    ) {
        var response = stationService.getStationsByAdministrator(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/stations/{id}/full
     * Obtener configuración completa de estación (islands, pumps, nozzles, schedules)
     * Para uso de la aplicación móvil
     */
    @GetMapping("/{id}/full")
    public ResponseEntity<StationDTO.StationFullConfigResponse> getStationFullConfiguration(
        @PathVariable Long id
    ) {
        try {
            var response = stationService.getStationFullConfiguration(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/stations/{id}/schedules
     * Obtener horarios de una estación
     */
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<StationDTO.ShiftScheduleResponse>> getStationSchedules(
        @PathVariable Long id
    ) {
        var response = stationService.getStationSchedules(id);
        return ResponseEntity.ok(response);
    }
}
