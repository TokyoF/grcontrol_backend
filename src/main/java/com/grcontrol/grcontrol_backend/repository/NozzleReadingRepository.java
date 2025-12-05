package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.NozzleReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para NozzleReading
 */
@Repository
public interface NozzleReadingRepository extends JpaRepository<NozzleReading, Long> {

    /**
     * Obtener todas las lecturas de una manguera en una sesión
     */
    List<NozzleReading> findByNozzleIdAndSessionId(Long nozzleId, Long sessionId);

    /**
     * Obtener una lectura específica por tipo
     */
    Optional<NozzleReading> findByNozzleIdAndSessionIdAndReadingType(
        Long nozzleId, 
        Long sessionId, 
        NozzleReading.ReadingType readingType
    );

    /**
     * Obtener todas las lecturas de una sesión
     */
    @Query("SELECT r FROM NozzleReading r " +
           "WHERE r.session.id = :sessionId " +
           "ORDER BY r.nozzle.id, r.readingType")
    List<NozzleReading> findAllBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Obtener lecturas de una sesión agrupadas por manguera
     */
    @Query("SELECT r FROM NozzleReading r " +
           "LEFT JOIN FETCH r.nozzle n " +
           "LEFT JOIN FETCH n.pump p " +
           "WHERE r.session.id = :sessionId " +
           "ORDER BY p.id, n.id, r.readingType")
    List<NozzleReading> findAllBySessionIdWithNozzleAndPump(@Param("sessionId") Long sessionId);

    /**
     * Verificar si existe una lectura
     */
    boolean existsByNozzleIdAndSessionIdAndReadingType(
        Long nozzleId, 
        Long sessionId, 
        NozzleReading.ReadingType readingType
    );

    /**
     * Obtener lecturas incompletas de una sesión
     */
    @Query("SELECT r FROM NozzleReading r " +
           "WHERE r.session.id = :sessionId " +
           "AND r.completed = false")
    List<NozzleReading> findIncompleteReadings(@Param("sessionId") Long sessionId);

    /**
     * Obtener lecturas modificadas de una sesión
     */
    @Query("SELECT r FROM NozzleReading r " +
           "WHERE r.session.id = :sessionId " +
           "AND r.wasModified = true")
    List<NozzleReading> findModifiedReadings(@Param("sessionId") Long sessionId);

    /**
     * Eliminar todas las lecturas de una sesión
     */
    void deleteBySessionId(Long sessionId);
}
