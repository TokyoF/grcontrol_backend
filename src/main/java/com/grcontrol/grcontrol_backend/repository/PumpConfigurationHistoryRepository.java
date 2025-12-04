package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.PumpConfigurationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad PumpConfigurationHistory
 */
@Repository
public interface PumpConfigurationHistoryRepository extends JpaRepository<PumpConfigurationHistory, Long> {

    /**
     * Buscar historial de una manguera ordenado por fecha descendente
     */
    List<PumpConfigurationHistory> findByNozzleIdOrderByChangeTimestampDesc(Long nozzleId);

    /**
     * Buscar cambios que afectaron a una sesión específica
     */
    @Query("SELECT pch FROM PumpConfigurationHistory pch " +
           "WHERE pch.affectedSession.id = :sessionId")
    List<PumpConfigurationHistory> findByAffectedSessionId(@Param("sessionId") Long sessionId);

    /**
     * Buscar cambios de un tipo específico para una manguera
     */
    List<PumpConfigurationHistory> findByNozzleIdAndChangeTypeOrderByChangeTimestampDesc(
        Long nozzleId,
        PumpConfigurationHistory.ConfigChangeType changeType
    );

    /**
     * Buscar cambios realizados por un usuario
     */
    List<PumpConfigurationHistory> findByChangedByIdOrderByChangeTimestampDesc(Long userId);

    /**
     * Buscar cambios en un rango de fechas
     */
    @Query("SELECT pch FROM PumpConfigurationHistory pch " +
           "WHERE pch.changeTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY pch.changeTimestamp DESC")
    List<PumpConfigurationHistory> findByChangeTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Buscar cambios de una manguera en un rango de fechas
     */
    @Query("SELECT pch FROM PumpConfigurationHistory pch " +
           "WHERE pch.nozzle.id = :nozzleId " +
           "AND pch.changeTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY pch.changeTimestamp DESC")
    List<PumpConfigurationHistory> findByNozzleIdAndDateRange(
        @Param("nozzleId") Long nozzleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Buscar cambios de todas las mangueras de un surtidor
     */
    @Query("SELECT pch FROM PumpConfigurationHistory pch " +
           "WHERE pch.nozzle.pump.id = :pumpId " +
           "ORDER BY pch.changeTimestamp DESC")
    List<PumpConfigurationHistory> findByPumpId(@Param("pumpId") Long pumpId);

    /**
     * Contar cambios de una manguera
     */
    long countByNozzleId(Long nozzleId);
}
