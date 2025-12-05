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
     * Incluye JOIN FETCH para cargar worker, island, station y shiftSchedule
     * NOTA: Puede retornar múltiples resultados si el trabajador tiene varios turnos el mismo día
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "LEFT JOIN FETCH wa.worker " +
           "LEFT JOIN FETCH wa.island i " +
           "LEFT JOIN FETCH i.station " +
           "LEFT JOIN FETCH wa.shiftSchedule " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate <= :date " +
           "AND :date < DATEADD(DAY, 7, wa.weekStartDate) " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE' " +
           "ORDER BY wa.shiftSchedule.startTime")
    List<WorkerAssignment> findActiveAssignments(
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
     * NOTA: Esta query ya no se usa para validación principal, solo para referencia
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
     * Verificar si un trabajador tiene un DÍA DE DESCANSO asignado para un día específico
     * Se usa para evitar asignar un turno activo cuando ya tiene descanso
     */
    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate = :weekStartDate " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE' " +
           "AND wa.isRestDay = true")
    boolean hasRestDayAssignment(
        @Param("workerId") Long workerId,
        @Param("weekStartDate") LocalDate weekStartDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * Verificar si un trabajador tiene TURNOS ACTIVOS (no descanso) para un día específico
     * Se usa para evitar asignar descanso cuando ya tiene turnos activos
     */
    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate = :weekStartDate " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE' " +
           "AND wa.isRestDay = false")
    boolean hasActiveShiftsForDay(
        @Param("workerId") Long workerId,
        @Param("weekStartDate") LocalDate weekStartDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * Obtener todas las asignaciones activas de un trabajador para un día específico
     * Útil para mostrar todos los turnos que tiene asignados
     */
    @Query("SELECT wa FROM WorkerAssignment wa " +
           "WHERE wa.worker.id = :workerId " +
           "AND wa.weekStartDate = :weekStartDate " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE' " +
           "ORDER BY wa.shiftSchedule.startTime")
    List<WorkerAssignment> findActiveAssignmentsForDay(
        @Param("workerId") Long workerId,
        @Param("weekStartDate") LocalDate weekStartDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * Verificar si ya existe un trabajador asignado a una isla/turno/día específico
     * (Para evitar que 2 griferos trabajen al mismo tiempo en la misma isla)
     */
    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkerAssignment wa " +
           "WHERE wa.island.id = :islandId " +
           "AND wa.shiftSchedule.id = :shiftScheduleId " +
           "AND wa.weekStartDate = :weekStartDate " +
           "AND wa.dayOfWeek = :dayOfWeek " +
           "AND wa.status = 'ACTIVE' " +
           "AND wa.isRestDay = false")
    boolean existsActiveAssignmentForIslandShiftDay(
        @Param("islandId") Long islandId,
        @Param("shiftScheduleId") Long shiftScheduleId,
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
