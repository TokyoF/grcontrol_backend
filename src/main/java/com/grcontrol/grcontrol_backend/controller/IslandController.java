package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.IslandDTO;
import com.grcontrol.grcontrol_backend.service.IslandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de islas
 */
@RestController
@RequestMapping("/api/islands")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IslandController {

    private final IslandService islandService;

    /**
     * POST /api/islands
     * Crear nueva isla
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<IslandDTO.IslandResponse> createIsland(
        @RequestBody IslandDTO.IslandRequest request
    ) {
        try {
            var response = islandService.createIsland(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/islands/{id}
     * Obtener isla por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<IslandDTO.IslandResponse> getIsland(
        @PathVariable Long id
    ) {
        try {
            var response = islandService.getIsland(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/islands/{id}/detail
     * Obtener isla con surtidores y mangueras
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<IslandDTO.IslandDetailResponse> getIslandDetail(
        @PathVariable Long id
    ) {
        try {
            var response = islandService.getIslandDetail(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/islands/by-station/{stationId}
     * Obtener islas de una estación
     */
    @GetMapping("/by-station/{stationId}")
    public ResponseEntity<List<IslandDTO.IslandResponse>> getIslandsByStation(
        @PathVariable Long stationId
    ) {
        var response = islandService.getIslandsByStation(stationId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/islands/by-station/{stationId}/active
     * Obtener solo islas activas de una estación
     */
    @GetMapping("/by-station/{stationId}/active")
    public ResponseEntity<List<IslandDTO.IslandResponse>> getActiveIslandsByStation(
        @PathVariable Long stationId
    ) {
        var response = islandService.getActiveIslandsByStation(stationId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/islands/{id}
     * Actualizar isla
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<IslandDTO.IslandResponse> updateIsland(
        @PathVariable Long id,
        @RequestBody IslandDTO.IslandRequest request
    ) {
        try {
            var response = islandService.updateIsland(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/islands/{id}/status
     * Cambiar estado de isla (ACTIVE, OFFLINE, MAINTENANCE)
     * Requiere rol ADMINISTRADOR
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<IslandDTO.OperationResponse> changeStatus(
        @PathVariable Long id,
        @RequestBody IslandDTO.ChangeStatusRequest request
    ) {
        try {
            var response = islandService.changeStatus(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/islands/{id}
     * Eliminar isla
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deleteIsland(@PathVariable Long id) {
        try {
            islandService.deleteIsland(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
