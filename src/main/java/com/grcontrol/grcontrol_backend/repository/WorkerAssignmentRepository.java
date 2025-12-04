package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad WorkerAssignment
 */
@Repository
public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {

    /**
     * Buscar asignación activa de un trabajador para un día específico
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate <= :date " +
           "AND :date < DATEADD(DAY, 7, wa.weekStartDate) " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE'")
    Optional<WorkerAssignment> findActiveAssignment(
        @Param("workerId") Long workerId,
        @Param("date") LocalDate date,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * Buscar todas las asignaciones de un trabajador para una semana
     */
    List<WorkerAssignment> findByWorkerIdAndWeekStartDate(Long workerId, LocalDate weekStartDate);

    /**
     * Buscar todas las asignaciones de una isla para una semana
     */
    List<WorkerAssignment> findByIslandIdAndWeekStartDate(Long islandId, LocalDate weekStartDate);

    /**
     * Buscar todas las asignaciones de una estación para una semana
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "WHERE wa.island.station.id = :stationId " +
           "AND wa.weekStartDate = :weekStartDate")
    List<WorkerAssignment> findByStationIdAndWeekStartDate(
        @Param("stationId") Long stationId,
        @Param("weekStartDate") LocalDate weekStartDate
    );

    /**
     * Buscar asignaciones activas de un trabajador
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.status = 'ACTIVE' " +
           "ORDER BY wa.weekStartDate DESC, wa.dayOfWeek")
    List<WorkerAssignment> findActiveByWorkerId(@Param("workerId") Long workerId);

    /**
     * Buscar asignaciones de un trabajador ordenadas por semana
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "ORDER BY wa.weekStartDate DESC, wa.dayOfWeek")
    List<WorkerAssignment> findByWorkerIdOrderByWeek(@Param("workerId") Long workerId);

    /**
     * Buscar asignaciones de una isla para un horario específico en una semana
     */
    List<WorkerAssignment> findByIslandIdAndShiftScheduleIdAndWeekStartDate(
        Long islandId,
        Long shiftScheduleId,
        LocalDate weekStartDate
    );

    /**
     * Verificar si existe asignación para un trabajador en un día específico
     */
    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate = :weekStartDate " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE'")
    boolean existsActiveAssignment(
        @Param("workerId") Long workerId,
        @Param("weekStartDate") LocalDate weekStartDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * Contar asignaciones activas de un trabajador
     */
    long countByWorkerIdAndStatus(Long workerId, WorkerAssignment.AssignmentStatus status);

    /**
     * Buscar asignaciones por estado
     */
    List<WorkerAssignment> findByStatus(WorkerAssignment.AssignmentStatus status);
}
