package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.PumpDTO;
import com.grcontrol.grcontrol_backend.service.PumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de surtidores
 */
@RestController
@RequestMapping("/api/pumps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PumpController {

    private final PumpService pumpService;

    /**
     * POST /api/pumps
     * Crear nuevo surtidor
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpDTO.PumpResponse> createPump(
        @RequestBody PumpDTO.PumpRequest request
    ) {
        try {
            var response = pumpService.createPump(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/pumps/{id}
     * Obtener surtidor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PumpDTO.PumpResponse> getPump(
        @PathVariable Long id
    ) {
        try {
            var response = pumpService.getPump(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/pumps/{id}/detail
     * Obtener surtidor con mangueras
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<PumpDTO.PumpDetailResponse> getPumpDetail(
        @PathVariable Long id
    ) {
        try {
            var response = pumpService.getPumpDetail(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/pumps/by-island/{islandId}
     * Obtener surtidores de una isla
     */
    @GetMapping("/by-island/{islandId}")
    public ResponseEntity<List<PumpDTO.PumpResponse>> getPumpsByIsland(
        @PathVariable Long islandId
    ) {
        var response = pumpService.getPumpsByIsland(islandId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pumps/by-island/{islandId}/active
     * Obtener solo surtidores activos de una isla
     */
    @GetMapping("/by-island/{islandId}/active")
    public ResponseEntity<List<PumpDTO.PumpResponse>> getActivePumpsByIsland(
        @PathVariable Long islandId
    ) {
        var response = pumpService.getActivePumpsByIsland(islandId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pumps/by-station/{stationId}/active
     * Obtener surtidores activos de toda una estación
     */
    @GetMapping("/by-station/{stationId}/active")
    public ResponseEntity<List<PumpDTO.PumpResponse>> getActivePumpsByStation(
        @PathVariable Long stationId
    ) {
        var response = pumpService.getActivePumpsByStation(stationId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/pumps/{id}
     * Actualizar surtidor
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpDTO.PumpResponse> updatePump(
        @PathVariable Long id,
        @RequestBody PumpDTO.PumpRequest request
    ) {
        try {
            var response = pumpService.updatePump(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/pumps/{id}/toggle-active
     * Activar/desactivar surtidor
     * Requiere rol ADMINISTRADOR
     */
    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpDTO.OperationResponse> toggleActive(
        @PathVariable Long id,
        @RequestBody PumpDTO.ToggleActiveRequest request
    ) {
        try {
            var response = pumpService.toggleActive(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/pumps/{id}
     * Eliminar surtidor
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deletePump(@PathVariable Long id) {
        try {
            pumpService.deletePump(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
