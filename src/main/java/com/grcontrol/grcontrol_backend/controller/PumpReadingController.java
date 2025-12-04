package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.PumpReadingDTO;
import com.grcontrol.grcontrol_backend.service.PumpReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de lecturas de bombas
 * Usa nozzleId en lugar de composite keys
 */
@RestController
@RequestMapping("/api/pump-readings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PumpReadingController {

    private final PumpReadingService pumpReadingService;

    /**
     * POST /api/pump-readings
     * Guardar nueva lectura de bomba
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpReadingDTO.ReadingResponse> createReading(
        @RequestBody PumpReadingDTO.CreateReadingRequest request
    ) {
        try {
            PumpReadingDTO.ReadingResponse response = pumpReadingService.createReading(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/pump-readings/batch
     * Guardar múltiples lecturas de una vez
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpReadingDTO.BatchResponse> createBatchReadings(
        @RequestBody PumpReadingDTO.BatchCreateRequest request
    ) {
        PumpReadingDTO.BatchResponse response = pumpReadingService.createBatchReadings(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/pump-readings/{id}
     * Obtener lectura por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<PumpReadingDTO.ReadingResponse> getReadingById(
        @PathVariable Long id
    ) {
        try {
            PumpReadingDTO.ReadingResponse response = pumpReadingService.getReadingById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/pump-readings/by-session/{sessionId}
     * Obtener todas las lecturas de una sesión
     */
    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<PumpReadingDTO.ReadingResponse>> getReadingsBySession(
        @PathVariable Long sessionId
    ) {
        List<PumpReadingDTO.ReadingResponse> readings = pumpReadingService.getReadingsBySession(sessionId);
        return ResponseEntity.ok(readings);
    }

    /**
     * GET /api/pump-readings/by-nozzle/{nozzleId}
     * Obtener lecturas de un nozzle específico
     */
    @GetMapping("/by-nozzle/{nozzleId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<List<PumpReadingDTO.ReadingResponse>> getReadingsByNozzle(
        @PathVariable Long nozzleId
    ) {
        List<PumpReadingDTO.ReadingResponse> readings = pumpReadingService.getReadingsByNozzle(nozzleId);
        return ResponseEntity.ok(readings);
    }

    /**
     * GET /api/pump-readings/last-completed
     * Obtener última lectura completada de un nozzle (para base readings)
     * Query params: nozzleId, readingType
     */
    @GetMapping("/last-completed")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpReadingDTO.BaseReadingResponse> getLastCompletedReading(
        @RequestParam Long nozzleId,
        @RequestParam String readingType
    ) {
        try {
            PumpReadingDTO.BaseReadingResponse response =
                pumpReadingService.getLastCompletedReading(nozzleId, readingType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/pump-readings/{id}
     * Actualizar una lectura existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpReadingDTO.ReadingResponse> updateReading(
        @PathVariable Long id,
        @RequestBody PumpReadingDTO.UpdateReadingRequest request
    ) {
        try {
            PumpReadingDTO.ReadingResponse response = pumpReadingService.updateReading(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/pump-readings/{id}
     * Eliminar una lectura (solo si no está completada)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<PumpReadingDTO.OperationResponse> deleteReading(
        @PathVariable Long id
    ) {
        try {
            pumpReadingService.deleteReading(id);
            return ResponseEntity.ok(
                new PumpReadingDTO.OperationResponse(
                    true,
                    "Lectura eliminada exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new PumpReadingDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
