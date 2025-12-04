package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Arqueo;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArqueoRepository extends JpaRepository<Arqueo, Long> {

    Optional<Arqueo> findByArqueoId(String arqueoId);

    Optional<Arqueo> findBySession(ShiftSession session);

    boolean existsBySession(ShiftSession session);

    // Nuevos métodos para ArqueoController
    @Query("SELECT a FROM Arqueo a WHERE a.session.id = :sessionId")
    Optional<Arqueo> findBySessionId(@Param("sessionId") Long sessionId);
}
