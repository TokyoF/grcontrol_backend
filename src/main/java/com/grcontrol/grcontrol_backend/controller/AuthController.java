package com.grcontrol.grcontrol_backend.controller;

import com.grcontrol.grcontrol_backend.dto.AuthResponse;
import com.grcontrol.grcontrol_backend.dto.LoginRequest;
import com.grcontrol.grcontrol_backend.dto.RegisterRequest;
import com.grcontrol.grcontrol_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, "ROLE_ADMINISTRADOR"));
    }

    @PostMapping("/register/grifero")
    public ResponseEntity<AuthResponse> registerGrifero(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, "ROLE_GRIFERO"));
    }

    @PostMapping("/register/facturador")
    public ResponseEntity<AuthResponse> registerFacturador(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, "ROLE_FACTURADOR"));
    }

    @PostMapping("/register/gerente")
    public ResponseEntity<AuthResponse> registerGerente(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, "ROLE_GERENTE"));
    }
}
