package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.PumpReading;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PumpReadingRepository extends JpaRepository<PumpReading, Long> {

    List<PumpReading> findBySessionOrderByCreatedAt(ShiftSession session);

    @Query("SELECT r FROM PumpReading r WHERE r.session = :session " +
           "AND r.readingType = :readingType")
    List<PumpReading> findBySessionAndReadingType(
        @Param("session") ShiftSession session,
        @Param("readingType") PumpReading.ReadingType readingType
    );

    @Query("SELECT r FROM PumpReading r WHERE r.session = :session " +
           "AND r.fuelType = :fuelType")
    List<PumpReading> findBySessionAndFuelType(
        @Param("session") ShiftSession session,
        @Param("fuelType") PumpReading.FuelType fuelType
    );

    @Query("SELECT SUM(r.difference) FROM PumpReading r WHERE r.session = :session " +
           "AND r.readingType = 'SOLES' AND r.completed = true")
    Double sumSalesBySession(@Param("session") ShiftSession session);

    @Query("SELECT SUM(r.difference) FROM PumpReading r WHERE r.session = :session " +
           "AND r.readingType = 'SOLES' AND r.fuelType = :fuelType AND r.completed = true")
    Double sumSalesBySessionAndFuelType(
        @Param("session") ShiftSession session,
        @Param("fuelType") PumpReading.FuelType fuelType
    );

    // Nuevos métodos para PumpReadingController (formato con nozzleId)
    @Query("SELECT r FROM PumpReading r WHERE r.session.id = :sessionId ORDER BY r.readingTimestamp DESC")
    List<PumpReading> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT r FROM PumpReading r WHERE r.nozzle.id = :nozzleId ORDER BY r.readingTimestamp DESC")
    List<PumpReading> findByNozzleId(@Param("nozzleId") Long nozzleId);

    @Query("SELECT r FROM PumpReading r WHERE r.nozzle.id = :nozzleId " +
           "AND r.readingType = :readingType AND r.completed = true " +
           "ORDER BY r.readingTimestamp DESC")
    List<PumpReading> findLastCompletedByNozzleAndType(
        @Param("nozzleId") Long nozzleId,
        @Param("readingType") PumpReading.ReadingType readingType
    );
}
