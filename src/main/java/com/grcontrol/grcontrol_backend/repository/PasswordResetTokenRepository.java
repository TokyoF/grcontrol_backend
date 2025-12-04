package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.PasswordResetToken;
import com.grcontrol.grcontrol_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Buscar token por usuario y código
     */
    Optional<PasswordResetToken> findByUserAndCode(User user, String code);

    /**
     * Buscar tokens válidos por usuario
     */
    List<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime now);

    /**
     * Eliminar tokens expirados
     */
    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    /**
     * Eliminar tokens por usuario
     */
    void deleteByUser(User user);
}
