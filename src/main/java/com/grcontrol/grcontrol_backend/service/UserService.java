package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.PasswordResetDTO;
import com.grcontrol.grcontrol_backend.dto.UserDTO;
import com.grcontrol.grcontrol_backend.entity.PasswordResetToken;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.repository.PasswordResetTokenRepository;
import com.grcontrol.grcontrol_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gestión de usuarios
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;

    /**
     * Obtener todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UserDTO.UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<UserDTO.UserResponse> getUsersByRole(String roleName) {
        return userRepository.findByRolesName(roleName).stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UserDTO.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToUserResponse(user);
    }

    /**
     * Obtener solo usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserDTO.UserResponse> getActiveUsers() {
        return userRepository.findByIsActive(true).stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * Desactivar usuario (soft delete)
     */
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivar usuario
     */
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Actualizar usuario (por admin)
     */
    @Transactional
    public UserDTO.UserResponse updateUser(Long id, UserDTO.UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        
        // Cambiar contraseña si se proporciona
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            
            // Enviar notificación
            try {
                emailService.sendPasswordChangedNotification(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName()
                );
            } catch (Exception e) {
                // Log pero no fallar la actualización
            }
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    /**
     * Solicitar código de recuperación de contraseña
     */
    @Transactional
    public PasswordResetDTO.PasswordResetResponse requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No existe usuario con ese email"));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUser(user);

        // Generar código de 6 dígitos
        String code = generateRandomCode();

        // Crear token
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        token.setUsed(false);
        tokenRepository.save(token);

        // Enviar email
        emailService.sendPasswordResetCode(
            user.getEmail(),
            user.getFirstName() + " " + user.getLastName(),
            code
        );

        return new PasswordResetDTO.PasswordResetResponse(
            true,
            "Código enviado a tu email. Válido por " + EXPIRY_MINUTES + " minutos."
        );
    }

    /**
     * Verificar código y cambiar contraseña
     */
    @Transactional
    public PasswordResetDTO.PasswordResetResponse resetPassword(
        String email, 
        String code, 
        String newPassword
    ) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email no encontrado"));

        PasswordResetToken token = tokenRepository.findByUserAndCode(user, code)
            .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (!token.isValid()) {
            throw new IllegalArgumentException(
                token.isExpired() ? "Código expirado" : "Código ya utilizado"
            );
        }

        // Cambiar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marcar token como usado
        token.setUsed(true);
        tokenRepository.save(token);

        // Enviar notificación
        try {
            emailService.sendPasswordChangedNotification(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
            );
        } catch (Exception e) {
            // Log pero no fallar
        }

        return new PasswordResetDTO.PasswordResetResponse(
            true,
            "Contraseña actualizada exitosamente"
        );
    }

    /**
     * Generar código aleatorio de 6 dígitos
     */
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6 dígitos
        return String.valueOf(code);
    }

    /**
     * Mapear User entity a UserResponse DTO
     */
    private UserDTO.UserResponse mapToUserResponse(User user) {
        return new UserDTO.UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getIsActive(),
            user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
