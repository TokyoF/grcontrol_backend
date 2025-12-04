package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.PasswordResetDTO;
import com.grcontrol.grcontrol_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para recuperación de contraseña
 * Endpoints públicos (no requieren autenticación)
 */
@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PasswordResetController {

    private final UserService userService;

    /**
     * POST /api/password-reset/request
     * Solicitar código de recuperación
     */
    @PostMapping("/request")
    public ResponseEntity<PasswordResetDTO.PasswordResetResponse> requestCode(
        @RequestBody PasswordResetDTO.RequestCodeRequest request
    ) {
        try {
            log.info("Solicitud de recuperación de contraseña para: {}", request.getEmail());
            PasswordResetDTO.PasswordResetResponse response = 
                userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error en solicitud de recuperación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new PasswordResetDTO.PasswordResetResponse(false, e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error inesperado en recuperación de contraseña", e);
            return ResponseEntity.internalServerError().body(
                new PasswordResetDTO.PasswordResetResponse(
                    false, 
                    "Error al procesar solicitud"
                )
            );
        }
    }

    /**
     * POST /api/password-reset/verify
     * Verificar código y cambiar contraseña
     */
    @PostMapping("/verify")
    public ResponseEntity<PasswordResetDTO.PasswordResetResponse> verifyAndReset(
        @RequestBody PasswordResetDTO.ResetPasswordRequest request
    ) {
        try {
            log.info("Verificación de código para: {}", request.getEmail());
            
            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(
                    new PasswordResetDTO.PasswordResetResponse(
                        false, 
                        "La contraseña debe tener al menos 6 caracteres"
                    )
                );
            }
            
            PasswordResetDTO.PasswordResetResponse response = 
                userService.resetPassword(
                    request.getEmail(),
                    request.getCode(),
                    request.getNewPassword()
                );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error en verificación de código: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new PasswordResetDTO.PasswordResetResponse(false, e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error inesperado al resetear contraseña", e);
            return ResponseEntity.internalServerError().body(
                new PasswordResetDTO.PasswordResetResponse(
                    false, 
                    "Error al procesar solicitud"
                )
            );
        }
    }
}
