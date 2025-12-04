package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Island;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Island
 */
@Repository
public interface IslandRepository extends JpaRepository<Island, Long> {

    /**
     * Buscar todas las islas activas de una estación
     */
    List<Island> findByStationIdAndActiveTrue(Long stationId);

    /**
     * Buscar todas las islas de una estación (activas e inactivas)
     */
    List<Island> findByStationId(Long stationId);

    /**
     * Buscar isla por ID con sus surtidores y mangueras precargados
     */
    @Query("SELECT DISTINCT i FROM Island i " +
           "LEFT JOIN FETCH i.station " +
           "LEFT JOIN FETCH i.pumps p " +
           "LEFT JOIN FETCH p.nozzles " +
           "WHERE i.id = :islandId")
    Optional<Island> findByIdWithPumpsAndNozzles(@Param("islandId") Long islandId);

    /**
     * Buscar islas por estación con surtidores precargados
     */
    @Query("SELECT DISTINCT i FROM Island i " +
           "LEFT JOIN FETCH i.station " +
           "LEFT JOIN FETCH i.pumps " +
           "WHERE i.station.id = :stationId AND i.active = true")
    List<Island> findByStationIdWithPumps(@Param("stationId") Long stationId);

    @Query("SELECT DISTINCT i FROM Island i " +
           "LEFT JOIN FETCH i.station " +
           "LEFT JOIN FETCH i.pumps " +
           "WHERE i.station.id = :stationId")
    List<Island> findAllByStationIdWithPumps(@Param("stationId") Long stationId);

    /**
     * Verificar si existe una isla con ese nombre en una estación
     */
    boolean existsByStationIdAndName(Long stationId, String name);

    /**
     * Contar islas activas de una estación
     */
    long countByStationIdAndActiveTrue(Long stationId);

    /**
     * Buscar islas por estación y estado
     */
    List<Island> findByStationIdAndStatus(Long stationId, Island.IslandStatus status);
}
