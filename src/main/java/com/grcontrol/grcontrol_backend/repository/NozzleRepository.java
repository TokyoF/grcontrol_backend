package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Nozzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Nozzle
 */
@Repository
public interface NozzleRepository extends JpaRepository<Nozzle, Long> {

    /**
     * Buscar mangueras de un surtidor ordenadas por posición
     */
    List<Nozzle> findByPumpIdOrderByPosition(Long pumpId);

    /**
     * Buscar mangueras activas de un surtidor
     */
    List<Nozzle> findByPumpIdAndActiveTrue(Long pumpId);

    /**
     * Buscar mangueras activas de una estación
     */
    @Query("SELECT n FROM Nozzle n " +
           "WHERE n.pump.island.station.id = :stationId AND n.active = true")
    List<Nozzle> findActiveNozzlesByStationId(@Param("stationId") Long stationId);

    /**
     * Buscar mangueras de un surtidor por lado
     */
    @Query("SELECT n FROM Nozzle n " +
           "WHERE n.pump.id = :pumpId AND n.side = :side " +
           "ORDER BY n.position")
    List<Nozzle> findByPumpIdAndSide(@Param("pumpId") Long pumpId,
                                      @Param("side") Nozzle.PumpSide side);

    /**
     * Buscar mangueras activas de una isla
     */
    @Query("SELECT n FROM Nozzle n " +
           "WHERE n.pump.island.id = :islandId AND n.active = true " +
           "ORDER BY n.pump.position, n.side, n.position")
    List<Nozzle> findActiveByIslandId(@Param("islandId") Long islandId);

    /**
     * Contar mangueras activas de un surtidor
     */
    long countByPumpIdAndActiveTrue(Long pumpId);

    /**
     * Verificar si existe una manguera con ese tipo de combustible y lado en un surtidor
     */
    boolean existsByPumpIdAndSideAndFuelType(Long pumpId,
                                              Nozzle.PumpSide side,
                                              Nozzle.FuelType fuelType);
}
