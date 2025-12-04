package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.FuelPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para FuelPriceHistory
 */
@Repository
public interface FuelPriceHistoryRepository extends JpaRepository<FuelPriceHistory, Long> {

    /**
     * Obtener el precio actual de un combustible en una estación
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "AND f.fuelType = :fuelType " +
           "AND f.effectiveUntil IS NULL")
    Optional<FuelPriceHistory> findCurrentPrice(
        @Param("stationId") Long stationId,
        @Param("fuelType") FuelPriceHistory.FuelType fuelType
    );

    /**
     * Obtener todos los precios actuales de una estación
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "AND f.effectiveUntil IS NULL")
    List<FuelPriceHistory> findAllCurrentPrices(@Param("stationId") Long stationId);

    /**
     * Obtener el precio vigente en una fecha específica
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "AND f.fuelType = :fuelType " +
           "AND f.effectiveFrom <= :dateTime " +
           "AND (f.effectiveUntil IS NULL OR f.effectiveUntil > :dateTime) " +
           "ORDER BY f.effectiveFrom DESC")
    Optional<FuelPriceHistory> findPriceAt(
        @Param("stationId") Long stationId,
        @Param("fuelType") FuelPriceHistory.FuelType fuelType,
        @Param("dateTime") LocalDateTime dateTime
    );

    /**
     * Obtener historial de precios de un combustible en una estación
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "AND f.fuelType = :fuelType " +
           "ORDER BY f.effectiveFrom DESC")
    List<FuelPriceHistory> findHistoryByStationAndFuelType(
        @Param("stationId") Long stationId,
        @Param("fuelType") FuelPriceHistory.FuelType fuelType
    );

    /**
     * Obtener historial completo de una estación
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "ORDER BY f.effectiveFrom DESC")
    List<FuelPriceHistory> findAllByStationId(@Param("stationId") Long stationId);

    /**
     * Obtener cambios de precio en un rango de fechas
     */
    @Query("SELECT f FROM FuelPriceHistory f " +
           "WHERE f.station.id = :stationId " +
           "AND f.fuelType = :fuelType " +
           "AND f.effectiveFrom BETWEEN :fromDate AND :toDate " +
           "ORDER BY f.effectiveFrom DESC")
    List<FuelPriceHistory> findPriceChangesBetween(
        @Param("stationId") Long stationId,
        @Param("fuelType") FuelPriceHistory.FuelType fuelType,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
}
