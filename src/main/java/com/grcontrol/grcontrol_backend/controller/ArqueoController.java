package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.ArqueoDTO;
import com.grcontrol.grcontrol_backend.service.ArqueoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestión de arqueos
 */
@RestController
@RequestMapping("/api/arqueos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArqueoController {

    private final ArqueoService arqueoService;

    /**
     * POST /api/arqueos
     * Crear nuevo arqueo
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ArqueoDTO.ArqueoResponse> createArqueo(
        @RequestBody ArqueoDTO.CreateArqueoRequest request
    ) {
        try {
            ArqueoDTO.ArqueoResponse response = arqueoService.createArqueo(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/arqueos/{id}
     * Obtener arqueo por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ArqueoDTO.ArqueoResponse> getArqueoById(
        @PathVariable Long id
    ) {
        try {
            ArqueoDTO.ArqueoResponse response = arqueoService.getArqueoById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/arqueos/by-session/{sessionId}
     * Obtener arqueo de una sesión
     */
    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ArqueoDTO.ArqueoResponse> getArqueoBySession(
        @PathVariable Long sessionId
    ) {
        try {
            ArqueoDTO.ArqueoResponse response = arqueoService.getArqueoBySession(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/arqueos/{id}
     * Actualizar arqueo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GRIFERO', 'ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<ArqueoDTO.ArqueoResponse> updateArqueo(
        @PathVariable Long id,
        @RequestBody ArqueoDTO.UpdateArqueoRequest request
    ) {
        try {
            ArqueoDTO.ArqueoResponse response = arqueoService.updateArqueo(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/arqueos/{id}/approve
     * Aprobar arqueo
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FACTURADOR')")
    public ResponseEntity<ArqueoDTO.OperationResponse> approveArqueo(
        @PathVariable Long id
    ) {
        try {
            arqueoService.approveArqueo(id);
            return ResponseEntity.ok(
                new ArqueoDTO.OperationResponse(
                    true,
                    "Arqueo aprobado exitosamente",
                    id
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ArqueoDTO.OperationResponse(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }
}
