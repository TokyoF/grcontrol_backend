package com.grcontrol.grcontrol_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PasswordResetDTO {

    /**
     * Request para solicitar código de recuperación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestCodeRequest {
        private String email;
    }

    /**
     * Request para verificar código y cambiar contraseña
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        private String email;
        private String code;
        private String newPassword;
    }

    /**
     * Response genérico
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetResponse {
        private Boolean success;
        private String message;
    }
}
