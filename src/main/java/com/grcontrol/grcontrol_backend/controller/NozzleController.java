package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.NozzleDTO;
import com.grcontrol.grcontrol_backend.service.NozzleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de mangueras y precios de combustible
 */
@RestController
@RequestMapping("/api/nozzles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NozzleController {

    private final NozzleService nozzleService;

    /**
     * POST /api/nozzles
     * Crear nueva manguera
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> createNozzle(
        @RequestBody NozzleDTO.NozzleRequest request
    ) {
        try {
            var response = nozzleService.createNozzle(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzles/{id}
     * Obtener manguera por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NozzleDTO.NozzleResponse> getNozzle(
        @PathVariable Long id
    ) {
        try {
            var response = nozzleService.getNozzle(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/nozzles/{id}/detail
     * Obtener manguera con información completa (estación, isla, surtidor)
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<NozzleDTO.NozzleDetailResponse> getNozzleDetail(
        @PathVariable Long id
    ) {
        try {
            var response = nozzleService.getNozzleDetail(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/nozzles/by-pump/{pumpId}
     * Obtener mangueras de un surtidor
     */
    @GetMapping("/by-pump/{pumpId}")
    public ResponseEntity<List<NozzleDTO.NozzleResponse>> getNozzlesByPump(
        @PathVariable Long pumpId
    ) {
        var response = nozzleService.getNozzlesByPump(pumpId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/nozzles/by-station/{stationId}/active
     * Obtener todas las mangueras activas de una estación
     * Útil para app móvil al iniciar turno
     */
    @GetMapping("/by-station/{stationId}/active")
    public ResponseEntity<List<NozzleDTO.NozzleResponse>> getActiveNozzlesByStation(
        @PathVariable Long stationId
    ) {
        var response = nozzleService.getActiveNozzlesByStation(stationId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/nozzles/{id}
     * Actualizar manguera
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<NozzleDTO.NozzleResponse> updateNozzle(
        @PathVariable Long id,
        @RequestBody NozzleDTO.NozzleRequest request
    ) {
        try {
            var response = nozzleService.updateNozzle(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== PRICE MANAGEMENT ====================

    /**
     * POST /api/nozzles/{id}/update-price
     * Actualizar precio de combustible (con auditoría automática)
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/{id}/update-price")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<NozzleDTO.OperationResponse> updatePrice(
        @PathVariable Long id,
        @RequestBody NozzleDTO.UpdatePriceRequest request
    ) {
        try {
            var response = nozzleService.updatePrice(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/nozzles/batch-update-prices
     * Actualizar múltiples precios a la vez
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/batch-update-prices")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<NozzleDTO.BatchOperationResponse> batchUpdatePrices(
        @RequestBody NozzleDTO.BatchUpdatePricesRequest request
    ) {
        var response = nozzleService.batchUpdatePrices(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/nozzles/{id}/toggle-active
     * Activar/desactivar manguera (con auditoría automática)
     * Requiere rol ADMINISTRADOR
     */
    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<NozzleDTO.OperationResponse> toggleActive(
        @PathVariable Long id,
        @RequestBody NozzleDTO.ToggleActiveRequest request
    ) {
        try {
            var response = nozzleService.toggleActive(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/nozzles/{id}/change-fuel-type
     * Cambiar tipo de combustible (con auditoría automática)
     * Requiere rol GERENTE
     */
    @PostMapping("/{id}/change-fuel-type")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<NozzleDTO.OperationResponse> changeFuelType(
        @PathVariable Long id,
        @RequestBody NozzleDTO.ChangeFuelTypeRequest request
    ) {
        try {
            var response = nozzleService.changeFuelType(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== DECIMAL CONFIGURATION ====================

    /**
     * GET /api/nozzles/{id}/decimals
     * Obtener configuración de decimales de una manguera
     */
    @GetMapping("/{id}/decimals")
    public ResponseEntity<?> getDecimalsConfig(@PathVariable Long id) {
        try {
            var response = nozzleService.getDecimalsConfig(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/nozzles/{id}/decimals
     * Actualizar configuración de decimales de una manguera
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}/decimals")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> updateDecimalsConfig(
        @PathVariable Long id,
        @RequestBody NozzleDTO.UpdateDecimalsRequest request
    ) {
        try {
            var response = nozzleService.updateDecimalsConfig(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    // ==================== COUNTER CONFIGURATION ====================

    /**
     * GET /api/nozzles/{id}/counter-config
     * Obtener configuración completa de contadores de una manguera
     */
    @GetMapping("/{id}/counter-config")
    public ResponseEntity<?> getCounterConfig(@PathVariable Long id) {
        try {
            var response = nozzleService.getCounterConfig(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/nozzles/{id}/counter-config
     * Actualizar configuración completa de contadores de una manguera
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PutMapping("/{id}/counter-config")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> updateCounterConfig(
        @PathVariable Long id,
        @RequestBody NozzleDTO.UpdateCounterConfigRequest request
    ) {
        try {
            var response = nozzleService.updateCounterConfig(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new NozzleDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * GET /api/nozzles/{id}/with-config
     * Obtener manguera con configuración completa de contadores
     */
    @GetMapping("/{id}/with-config")
    public ResponseEntity<?> getNozzleWithConfig(@PathVariable Long id) {
        try {
            var response = nozzleService.getNozzleWithConfig(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/nozzles/pump/{pumpId}/with-config
     * Obtener todas las mangueras de un surtidor con configuración
     */
    @GetMapping("/pump/{pumpId}/with-config")
    public ResponseEntity<?> getNozzlesWithConfigByPump(@PathVariable Long pumpId) {
        var response = nozzleService.getNozzlesWithConfigByPump(pumpId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/nozzles/island/{islandId}/with-config
     * Obtener todas las mangueras de una isla con configuración
     * Útil para la app móvil del grifero
     */
    @GetMapping("/island/{islandId}/with-config")
    public ResponseEntity<?> getNozzlesWithConfigByIsland(@PathVariable Long islandId) {
        var response = nozzleService.getNozzlesWithConfigByIsland(islandId);
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE ====================

    /**
     * DELETE /api/nozzles/{id}
     * Eliminar manguera
     * Requiere rol GERENTE
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deleteNozzle(@PathVariable Long id) {
        try {
            nozzleService.deleteNozzle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
