package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.PumpConfigDTO;
import com.grcontrol.grcontrol_backend.service.PumpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para administración de configuración de surtidores y precios
 * Solo accesible para rol ADMINISTRADOR
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminConfigController {

    private final PumpConfigService pumpConfigService;

    // ==================== GESTIÓN DE ISLAS ====================

    /**
     * GET /api/admin/config/stations/{stationId}/islands
     * Obtener todas las islas de una estación
     */
    @GetMapping("/stations/{stationId}/islands")
    public ResponseEntity<List<PumpConfigDTO.IslandResponse>> getIslandsByStation(
            @PathVariable Long stationId) {
        List<PumpConfigDTO.IslandResponse> islands = pumpConfigService.getAllIslandsByStation(stationId);
        return ResponseEntity.ok(islands);
    }

    /**
     * GET /api/admin/config/islands/{islandId}
     * Obtener una isla por ID
     */
    @GetMapping("/islands/{islandId}")
    public ResponseEntity<PumpConfigDTO.IslandResponse> getIsland(@PathVariable Long islandId) {
        PumpConfigDTO.IslandResponse island = pumpConfigService.getIslandById(islandId);
        return ResponseEntity.ok(island);
    }

    /**
     * POST /api/admin/config/stations/{stationId}/islands
     * Crear una nueva isla
     */
    @PostMapping("/stations/{stationId}/islands")
    public ResponseEntity<PumpConfigDTO.IslandResponse> createIsland(
            @PathVariable Long stationId,
            @RequestBody PumpConfigDTO.IslandRequest request) {
        PumpConfigDTO.IslandResponse island = pumpConfigService.createIsland(stationId, request);
        return ResponseEntity.ok(island);
    }

    /**
     * PUT /api/admin/config/islands/{islandId}
     * Actualizar una isla
     */
    @PutMapping("/islands/{islandId}")
    public ResponseEntity<PumpConfigDTO.IslandResponse> updateIsland(
            @PathVariable Long islandId,
            @RequestBody PumpConfigDTO.IslandRequest request) {
        PumpConfigDTO.IslandResponse island = pumpConfigService.updateIsland(islandId, request);
        return ResponseEntity.ok(island);
    }

    // ==================== GESTIÓN DE SURTIDORES ====================

    /**
     * GET /api/admin/config/islands/{islandId}/pumps
     * Obtener todos los surtidores de una isla
     */
    @GetMapping("/islands/{islandId}/pumps")
    public ResponseEntity<List<PumpConfigDTO.PumpResponse>> getPumpsByIsland(
            @PathVariable Long islandId) {
        List<PumpConfigDTO.PumpResponse> pumps = pumpConfigService.getAllPumpsByIsland(islandId);
        return ResponseEntity.ok(pumps);
    }

    /**
     * GET /api/admin/config/pumps/{pumpId}
     * Obtener un surtidor por ID
     */
    @GetMapping("/pumps/{pumpId}")
    public ResponseEntity<PumpConfigDTO.PumpResponse> getPump(@PathVariable Long pumpId) {
        PumpConfigDTO.PumpResponse pump = pumpConfigService.getPumpById(pumpId);
        return ResponseEntity.ok(pump);
    }

    /**
     * POST /api/admin/config/pumps
     * Crear un nuevo surtidor
     */
    @PostMapping("/pumps")
    public ResponseEntity<PumpConfigDTO.PumpResponse> createPump(
            @RequestBody PumpConfigDTO.PumpRequest request) {
        PumpConfigDTO.PumpResponse pump = pumpConfigService.createPump(request);
        return ResponseEntity.ok(pump);
    }

    /**
     * PUT /api/admin/config/pumps/{pumpId}
     * Actualizar un surtidor
     */
    @PutMapping("/pumps/{pumpId}")
    public ResponseEntity<PumpConfigDTO.PumpResponse> updatePump(
            @PathVariable Long pumpId,
            @RequestBody PumpConfigDTO.PumpRequest request) {
        PumpConfigDTO.PumpResponse pump = pumpConfigService.updatePump(pumpId, request);
        return ResponseEntity.ok(pump);
    }

    /**
     * DELETE /api/admin/config/pumps/{pumpId}
     * Eliminar un surtidor
     */
    @DeleteMapping("/pumps/{pumpId}")
    public ResponseEntity<PumpConfigDTO.OperationResponse> deletePump(@PathVariable Long pumpId) {
        pumpConfigService.deletePump(pumpId);
        return ResponseEntity.ok(new PumpConfigDTO.OperationResponse(
            true, "Surtidor eliminado exitosamente", pumpId
        ));
    }

    // ==================== GESTIÓN DE MANGUERAS ====================

    /**
     * GET /api/admin/config/pumps/{pumpId}/nozzles
     * Obtener todas las mangueras de un surtidor
     */
    @GetMapping("/pumps/{pumpId}/nozzles")
    public ResponseEntity<List<PumpConfigDTO.NozzleConfigResponse>> getNozzlesByPump(
            @PathVariable Long pumpId) {
        List<PumpConfigDTO.NozzleConfigResponse> nozzles = pumpConfigService.getAllNozzlesByPump(pumpId);
        return ResponseEntity.ok(nozzles);
    }

    /**
     * GET /api/admin/config/nozzles/{nozzleId}
     * Obtener una manguera por ID
     */
    @GetMapping("/nozzles/{nozzleId}")
    public ResponseEntity<PumpConfigDTO.NozzleConfigResponse> getNozzle(@PathVariable Long nozzleId) {
        PumpConfigDTO.NozzleConfigResponse nozzle = pumpConfigService.getNozzleById(nozzleId);
        return ResponseEntity.ok(nozzle);
    }

    /**
     * POST /api/admin/config/pumps/{pumpId}/nozzles
     * Crear una nueva manguera en un surtidor
     */
    @PostMapping("/pumps/{pumpId}/nozzles")
    public ResponseEntity<PumpConfigDTO.NozzleConfigResponse> createNozzle(
            @PathVariable Long pumpId,
            @RequestBody PumpConfigDTO.NozzleConfigRequest request) {
        PumpConfigDTO.NozzleConfigResponse nozzle = pumpConfigService.createNozzle(pumpId, request);
        return ResponseEntity.ok(nozzle);
    }

    /**
     * PUT /api/admin/config/nozzles/{nozzleId}
     * Actualizar configuración de una manguera
     * Permite modificar: tipo combustible, contadores (dígitos/decimales), precios
     */
    @PutMapping("/nozzles/{nozzleId}")
    public ResponseEntity<PumpConfigDTO.NozzleConfigResponse> updateNozzle(
            @PathVariable Long nozzleId,
            @RequestBody PumpConfigDTO.NozzleConfigRequest request) {
        PumpConfigDTO.NozzleConfigResponse nozzle = pumpConfigService.updateNozzle(nozzleId, request);
        return ResponseEntity.ok(nozzle);
    }

    /**
     * DELETE /api/admin/config/nozzles/{nozzleId}
     * Eliminar una manguera
     */
    @DeleteMapping("/nozzles/{nozzleId}")
    public ResponseEntity<PumpConfigDTO.OperationResponse> deleteNozzle(@PathVariable Long nozzleId) {
        pumpConfigService.deleteNozzle(nozzleId);
        return ResponseEntity.ok(new PumpConfigDTO.OperationResponse(
            true, "Manguera eliminada exitosamente", nozzleId
        ));
    }

    // ==================== GESTIÓN DE PRECIOS ====================

    /**
     * GET /api/admin/config/stations/{stationId}/prices
     * Obtener precios actuales de todos los combustibles
     */
    @GetMapping("/stations/{stationId}/prices")
    public ResponseEntity<List<PumpConfigDTO.FuelPriceResponse>> getCurrentPrices(
            @PathVariable Long stationId) {
        List<PumpConfigDTO.FuelPriceResponse> prices = pumpConfigService.getCurrentPrices(stationId);
        return ResponseEntity.ok(prices);
    }

    /**
     * GET /api/admin/config/stations/{stationId}/prices/{fuelType}/history
     * Obtener historial de precios de un combustible
     */
    @GetMapping("/stations/{stationId}/prices/{fuelType}/history")
    public ResponseEntity<List<PumpConfigDTO.FuelPriceResponse>> getPriceHistory(
            @PathVariable Long stationId,
            @PathVariable String fuelType) {
        List<PumpConfigDTO.FuelPriceResponse> history = pumpConfigService.getPriceHistory(stationId, fuelType);
        return ResponseEntity.ok(history);
    }

    /**
     * POST /api/admin/config/stations/{stationId}/prices
     * Actualizar precio de un combustible
     * El precio anterior se marca como inactivo y se crea un nuevo registro
     */
    @PostMapping("/stations/{stationId}/prices")
    public ResponseEntity<PumpConfigDTO.FuelPriceResponse> updateFuelPrice(
            @PathVariable Long stationId,
            @RequestBody PumpConfigDTO.FuelPriceUpdateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        PumpConfigDTO.FuelPriceResponse price = pumpConfigService.updateFuelPrice(
            stationId, request, username
        );
        return ResponseEntity.ok(price);
    }
}
