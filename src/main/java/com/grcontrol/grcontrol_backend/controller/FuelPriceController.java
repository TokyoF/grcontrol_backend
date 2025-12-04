package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.FuelPriceDTO;
import com.grcontrol.grcontrol_backend.service.FuelPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de precios de combustibles
 */
@RestController
@RequestMapping("/api/fuel-prices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;

    // ==================== CONSULTAS (Acceso público para operarios) ====================

    /**
     * GET /api/fuel-prices/current/{stationId}/{fuelType}
     * Obtener precio actual de un combustible
     */
    @GetMapping("/current/{stationId}/{fuelType}")
    public ResponseEntity<FuelPriceDTO.CurrentPriceResponse> getCurrentPrice(
        @PathVariable Long stationId,
        @PathVariable String fuelType
    ) {
        try {
            var response = fuelPriceService.getCurrentPrice(stationId, fuelType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/fuel-prices/current/{stationId}
     * Obtener todos los precios actuales de una estación
     */
    @GetMapping("/current/{stationId}")
    public ResponseEntity<FuelPriceDTO.StationCurrentPricesResponse> getAllCurrentPrices(
        @PathVariable Long stationId
    ) {
        try {
            var response = fuelPriceService.getAllCurrentPrices(stationId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/fuel-prices/history/{stationId}/{fuelType}
     * Obtener historial de precios de un combustible
     */
    @GetMapping("/history/{stationId}/{fuelType}")
    public ResponseEntity<List<FuelPriceDTO.PriceHistoryResponse>> getPriceHistory(
        @PathVariable Long stationId,
        @PathVariable String fuelType
    ) {
        try {
            var response = fuelPriceService.getPriceHistory(stationId, fuelType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/fuel-prices/history/{stationId}/{fuelType}/range
     * Obtener cambios de precio en un rango de fechas
     */
    @GetMapping("/history/{stationId}/{fuelType}/range")
    public ResponseEntity<List<FuelPriceDTO.PriceHistoryResponse>> getPriceChangesBetween(
        @PathVariable Long stationId,
        @PathVariable String fuelType,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
    ) {
        try {
            var response = fuelPriceService.getPriceChangesBetween(
                stationId, fuelType, fromDate, toDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== OPERACIONES (Solo ADMIN/GERENTE) ====================

    /**
     * POST /api/fuel-prices/update
     * Actualizar precio de un combustible
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<FuelPriceDTO.OperationResponse> updatePrice(
        @RequestBody FuelPriceDTO.UpdatePriceRequest request
    ) {
        try {
            var response = fuelPriceService.updatePrice(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new FuelPriceDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null,
                    null
                )
            );
        }
    }

    /**
     * POST /api/fuel-prices/initial
     * Establecer precio inicial para un combustible
     * Requiere rol ADMINISTRADOR o GERENTE
     */
    @PostMapping("/initial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<FuelPriceDTO.OperationResponse> setInitialPrice(
        @RequestBody FuelPriceDTO.SetInitialPriceRequest request
    ) {
        try {
            var response = fuelPriceService.setInitialPrice(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new FuelPriceDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null,
                    null
                )
            );
        }
    }
}
