package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ShiftSchedule
 */
@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    /**
     * Buscar horarios activos de una estación
     */
    List<ShiftSchedule> findByStationIdAndActiveTrue(Long stationId);

    /**
     * Buscar todos los horarios de una estación
     */
    List<ShiftSchedule> findByStationId(Long stationId);

    /**
     * Buscar horario por estación y etiqueta de visualización
     */
    Optional<ShiftSchedule> findByStationIdAndDisplayLabel(Long stationId, String displayLabel);

    /**
     * Buscar horario por estación y nombre
     */
    Optional<ShiftSchedule> findByStationIdAndName(Long stationId, String name);

    /**
     * Verificar si existe un horario con ese nombre en una estación
     */
    boolean existsByStationIdAndName(Long stationId, String name);

    /**
     * Verificar si existe un horario con esa etiqueta en una estación
     */
    boolean existsByStationIdAndDisplayLabel(Long stationId, String displayLabel);

    /**
     * Contar horarios activos de una estación
     */
    long countByStationIdAndActiveTrue(Long stationId);
}
