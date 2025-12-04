package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Pump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pump
 */
@Repository
public interface PumpRepository extends JpaRepository<Pump, Long> {

    /**
     * Buscar todos los surtidores activos de una isla
     */
    List<Pump> findByIslandIdAndActiveTrue(Long islandId);

    /**
     * Buscar todos los surtidores de una isla (activos e inactivos)
     */
    List<Pump> findByIslandId(Long islandId);

    /**
     * Buscar surtidor por ID con sus mangueras precargadas
     */
    @Query("SELECT p FROM Pump p LEFT JOIN FETCH p.nozzles WHERE p.id = :pumpId")
    Optional<Pump> findByIdWithNozzles(@Param("pumpId") Long pumpId);

    /**
     * Buscar surtidores de una isla con mangueras precargadas
     */
    @Query("SELECT DISTINCT p FROM Pump p " +
           "LEFT JOIN FETCH p.nozzles " +
           "WHERE p.island.id = :islandId")
    List<Pump> findByIslandIdWithNozzles(@Param("islandId") Long islandId);

    /**
     * Buscar todos los surtidores de una estación
     */
    @Query("SELECT p FROM Pump p WHERE p.island.station.id = :stationId AND p.active = true")
    List<Pump> findActiveByStationId(@Param("stationId") Long stationId);

    /**
     * Contar surtidores activos de una isla
     */
    long countByIslandIdAndActiveTrue(Long islandId);
}
