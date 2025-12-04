package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftSessionRepository extends JpaRepository<ShiftSession, Long> {

    Optional<ShiftSession> findBySessionId(String sessionId);

    List<ShiftSession> findByOperatorAndStatusOrderByStartTimeDesc(
        User operator,
        ShiftSession.SessionStatus status
    );

    @Query("SELECT s FROM ShiftSession s WHERE s.operator = :operator " +
           "AND s.startTime >= :startDate AND s.startTime < :endDate " +
           "ORDER BY s.startTime DESC")
    List<ShiftSession> findByOperatorAndDateRange(
        @Param("operator") User operator,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT s FROM ShiftSession s WHERE s.syncStatus = :syncStatus")
    List<ShiftSession> findBySyncStatus(@Param("syncStatus") ShiftSession.SyncStatus syncStatus);

    boolean existsBySessionId(String sessionId);

    // Nuevos métodos para ShiftSessionController
    @Query("SELECT s FROM ShiftSession s WHERE s.operator.id = :workerId ORDER BY s.startTime DESC")
    List<ShiftSession> findByWorkerId(@Param("workerId") Long workerId);

    @Query("SELECT s FROM ShiftSession s WHERE s.status = 'ACTIVE' ORDER BY s.startTime DESC")
    List<ShiftSession> findActiveSessions();

    @Query("SELECT s FROM ShiftSession s WHERE s.station.id = :stationId ORDER BY s.startTime DESC")
    List<ShiftSession> findByStationId(@Param("stationId") Long stationId);
}
