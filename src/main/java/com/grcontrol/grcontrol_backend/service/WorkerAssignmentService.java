package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.WorkerAssignmentDTO;
import com.grcontrol.grcontrol_backend.entity.*;
import com.grcontrol.grcontrol_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de asignaciones de trabajadores a islas
 * Maneja la programación semanal de turnos
 */
@Service
@RequiredArgsConstructor
public class WorkerAssignmentService {

    private final WorkerAssignmentRepository workerAssignmentRepository;
    private final UserRepository userRepository;
    private final IslandRepository islandRepository;
    private final ShiftScheduleRepository shiftScheduleRepository;
    private final StationRepository stationRepository;

    // ==================== CREATE OPERATIONS ====================

    /**
     * Crear asignación individual
     */
    @Transactional
    public WorkerAssignmentDTO.AssignmentResponse createAssignment(
            WorkerAssignmentDTO.AssignmentRequest request,
            Long createdByUserId) {
        
        User worker = userRepository.findById(request.workerId())
            .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con id: " + request.workerId()));

        // Verificar que el usuario tenga rol GRIFERO (con o sin prefijo ROLE_)
        boolean isGrifero = worker.getRoles().stream()
            .anyMatch(role -> role.getName().equals("GRIFERO") || role.getName().equals("ROLE_GRIFERO"));
        if (!isGrifero) {
            throw new IllegalArgumentException("El usuario debe tener rol GRIFERO para ser asignado a un turno");
        }

        Island island = islandRepository.findById(request.islandId())
            .orElseThrow(() -> new IllegalArgumentException("Isla no encontrada con id: " + request.islandId()));

        ShiftSchedule shiftSchedule = shiftScheduleRepository.findById(request.shiftScheduleId())
            .orElseThrow(() -> new IllegalArgumentException("Horario de turno no encontrado con id: " + request.shiftScheduleId()));

        // Validar que la fecha sea lunes
        LocalDate weekStart = request.weekStartDate();
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        // NUEVA LÓGICA: Un trabajador puede tener múltiples turnos el mismo día, 
        // PERO NO puede tener descanso + turno activo al mismo tiempo

        if (request.isRestDay()) {
            // Si estamos asignando un DESCANSO, verificar que NO tenga turnos activos ese día
            if (workerAssignmentRepository.hasActiveShiftsForDay(
                    request.workerId(), weekStart, request.dayOfWeek())) {
                throw new IllegalArgumentException(
                    String.format("El trabajador ya tiene turnos activos para %s. No se puede asignar descanso y turno activo el mismo día.",
                        getDayNameInSpanish(request.dayOfWeek()))
                );
            }
        } else {
            // Si estamos asignando un TURNO ACTIVO, verificar que NO tenga descanso ese día
            if (workerAssignmentRepository.hasRestDayAssignment(
                    request.workerId(), weekStart, request.dayOfWeek())) {
                throw new IllegalArgumentException(
                    String.format("El trabajador tiene asignado descanso para %s. No se puede asignar turno activo y descanso el mismo día.",
                        getDayNameInSpanish(request.dayOfWeek()))
                );
            }
        }

        // Verificar si ya hay OTRO trabajador asignado a esa isla/turno/día (no permitir duplicados)
        // IMPORTANTE: Solo rechazar si es un trabajador DIFERENTE al que estamos asignando
        if (!request.isRestDay()) {
            List<WorkerAssignment> existingAssignments = workerAssignmentRepository
                .findByIslandIdAndShiftScheduleIdAndWeekStartDate(
                    request.islandId(), 
                    request.shiftScheduleId(), 
                    weekStart
                );
            
            // Filtrar solo asignaciones activas del mismo día
            boolean hasOtherWorker = existingAssignments.stream()
                .anyMatch(wa -> 
                    wa.getStatus() == WorkerAssignment.AssignmentStatus.ACTIVE &&
                    wa.getDayOfWeek() == request.dayOfWeek() &&
                    !wa.getWorker().getId().equals(request.workerId())  // Excluir al mismo trabajador
                );
            
            if (hasOtherWorker) {
                throw new IllegalArgumentException(
                    String.format("Ya existe otro trabajador asignado a %s en el turno %s para %s",
                        island.getName(), 
                        shiftSchedule.getDisplayLabel(), 
                        getDayNameInSpanish(request.dayOfWeek()))
                );
            }
        }

        WorkerAssignment assignment = new WorkerAssignment();
        assignment.setWorker(worker);
        assignment.setIsland(island);
        assignment.setShiftSchedule(shiftSchedule);
        assignment.setWeekStartDate(weekStart);
        assignment.setDayOfWeek(request.dayOfWeek());
        assignment.setRestDay(request.isRestDay() != null ? request.isRestDay() : false);
        assignment.setNotes(request.notes());
        assignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);

        if (createdByUserId != null) {
            User createdBy = userRepository.findById(createdByUserId).orElse(null);
            assignment.setCreatedBy(createdBy);
        }

        assignment = workerAssignmentRepository.save(assignment);

        return toAssignmentResponse(assignment);
    }

    /**
     * Crear asignaciones semanales completas (7 días)
     */
    @Transactional
    public WorkerAssignmentDTO.BatchOperationResponse createWeeklyAssignments(
            WorkerAssignmentDTO.WeeklyAssignmentRequest request,
            Long createdByUserId) {
        
        User worker = userRepository.findById(request.workerId())
            .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con id: " + request.workerId()));

        // Validar que sea lunes
        LocalDate weekStart = request.weekStartDate();
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        List<Long> assignmentIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int created = 0;

        for (WorkerAssignmentDTO.DayAssignment dayAssignment : request.assignments()) {
            try {
                WorkerAssignmentDTO.AssignmentRequest singleRequest = new WorkerAssignmentDTO.AssignmentRequest(
                    request.workerId(),
                    dayAssignment.islandId(),
                    dayAssignment.shiftScheduleId(),
                    weekStart,
                    dayAssignment.dayOfWeek(),
                    dayAssignment.isRestDay(),
                    null
                );

                var response = createAssignment(singleRequest, createdByUserId);
                assignmentIds.add(response.id());
                created++;
            } catch (Exception e) {
                errors.add(String.format("%s: %s", 
                    getDayNameInSpanish(dayAssignment.dayOfWeek()), 
                    e.getMessage()));
            }
        }

        String message = String.format("Se crearon %d asignaciones", created);
        if (!errors.isEmpty()) {
            message += String.format(", %d errores", errors.size());
        }

        return new WorkerAssignmentDTO.BatchOperationResponse(
            errors.isEmpty(),
            message,
            created,
            assignmentIds,
            errors
        );
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Obtener asignación por ID
     */
    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.AssignmentResponse getAssignment(Long id) {
        WorkerAssignment assignment = workerAssignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con id: " + id));

        return toAssignmentResponse(assignment);
    }

    /**
     * Obtener asignaciones semanales de un trabajador
     */
    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.WeeklyAssignmentResponse getWorkerWeeklyAssignments(
            Long workerId, 
            LocalDate weekStartDate) {
        
        User worker = userRepository.findById(workerId)
            .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con id: " + workerId));

        // Asegurar que sea lunes
        LocalDate weekStart = weekStartDate;
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        List<WorkerAssignment> assignments = workerAssignmentRepository
            .findByWorkerIdAndWeekStartDate(workerId, weekStart);

        // Filtrar solo asignaciones ACTIVAS
        List<WorkerAssignment> activeAssignments = assignments.stream()
            .filter(a -> a.getStatus() == WorkerAssignment.AssignmentStatus.ACTIVE)
            .collect(Collectors.toList());

        List<WorkerAssignmentDTO.AssignmentResponse> responses = activeAssignments.stream()
            .map(this::toAssignmentResponse)
            .sorted(Comparator.comparing(WorkerAssignmentDTO.AssignmentResponse::dayOfWeek))
            .collect(Collectors.toList());

        long restDays = activeAssignments.stream().filter(a -> Boolean.TRUE.equals(a.getRestDay())).count();
        long workDays = activeAssignments.size() - restDays;

        LocalDate weekEnd = weekStart.plusDays(6);

        return new WorkerAssignmentDTO.WeeklyAssignmentResponse(
            workerId,
            worker.getUsername(),
            weekStart,
            weekEnd,
            responses,
            (int) restDays,
            (int) workDays
        );
    }

    /**
     * Obtener horario semanal de una isla
     */
    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.IslandWeeklyScheduleResponse getIslandWeeklySchedule(
            Long islandId,
            LocalDate weekStartDate) {
        
        Island island = islandRepository.findById(islandId)
            .orElseThrow(() -> new IllegalArgumentException("Isla no encontrada con id: " + islandId));

        // Asegurar que sea lunes
        LocalDate weekStart = weekStartDate;
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        List<WorkerAssignment> assignments = workerAssignmentRepository
            .findByIslandIdAndWeekStartDate(islandId, weekStart);

        // Filtrar solo asignaciones ACTIVAS (no incluir REPLACED, CANCELLED, etc.)
        List<WorkerAssignment> activeAssignments = assignments.stream()
            .filter(a -> a.getStatus() == WorkerAssignment.AssignmentStatus.ACTIVE)
            .collect(Collectors.toList());

        // Agrupar por día
        Map<DayOfWeek, List<WorkerAssignment>> byDay = activeAssignments.stream()
            .collect(Collectors.groupingBy(WorkerAssignment::getDayOfWeek));

        // Crear schedule para cada día
        List<WorkerAssignmentDTO.DaySchedule> schedule = Arrays.stream(DayOfWeek.values())
            .map(day -> {
                List<WorkerAssignment> dayAssignments = byDay.getOrDefault(day, Collections.emptyList());
                List<WorkerAssignmentDTO.ShiftAssignment> shifts = dayAssignments.stream()
                    .map(this::toShiftAssignment)
                    .collect(Collectors.toList());
                return new WorkerAssignmentDTO.DaySchedule(day, shifts);
            })
            .collect(Collectors.toList());

        LocalDate weekEnd = weekStart.plusDays(6);

        return new WorkerAssignmentDTO.IslandWeeklyScheduleResponse(
            islandId,
            island.getName(),
            weekStart,
            weekEnd,
            schedule
        );
    }

    /**
     * Obtener asignación activa de un trabajador para una fecha específica
     */
    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.ActiveAssignmentResponse getActiveAssignmentForDate(
            Long workerId,
            LocalDate date) {
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        List<WorkerAssignment> assignments = workerAssignmentRepository
            .findActiveAssignments(workerId, date, dayOfWeek);

        if (assignments.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("No hay asignación activa para el trabajador en la fecha %s", date)
            );
        }

        // Si hay múltiples asignaciones, tomar la primera (ordenada por hora de inicio)
        WorkerAssignment a = assignments.get(0);
        
        // Manejar campos que pueden ser null si es día de descanso
        Long shiftScheduleId = a.getShiftSchedule() != null ? a.getShiftSchedule().getId() : null;
        String shiftName = a.getShiftSchedule() != null ? a.getShiftSchedule().getName() : "DESCANSO";
        String shiftDisplayLabel = a.getShiftSchedule() != null ? a.getShiftSchedule().getDisplayLabel() : "Descanso";
        String startTime = a.getShiftSchedule() != null ? a.getShiftSchedule().getStartTime().toString() : null;
        String endTime = a.getShiftSchedule() != null ? a.getShiftSchedule().getEndTime().toString() : null;
        Boolean isOvernight = a.getShiftSchedule() != null && a.getShiftSchedule().getIsOvernight();
        
        Long islandId = a.getIsland() != null ? a.getIsland().getId() : null;
        String islandName = a.getIsland() != null ? a.getIsland().getName() : "Sin asignación";
        
        Long stationId = null;
        String stationName = null;
        if (a.getIsland() != null && a.getIsland().getStation() != null) {
            stationId = a.getIsland().getStation().getId();
            stationName = a.getIsland().getStation().getName();
        }
        
        return new WorkerAssignmentDTO.ActiveAssignmentResponse(
            a.getId(),
            a.getWorker().getId(),
            a.getWorker().getUsername(),
            islandId,
            islandName,
            stationId,
            stationName,
            shiftScheduleId,
            shiftName,
            shiftDisplayLabel,
            startTime,
            endTime,
            isOvernight,
            date,
            dayOfWeek,
            a.getWeekStartDate(),
            a.getRestDay()
        );
    }

    /**
     * Obtener horario semanal completo de una estación (todas las islas)
     */
    @Transactional(readOnly = true)
    public List<WorkerAssignmentDTO.IslandWeeklyScheduleResponse> getStationWeeklySchedule(
            Long stationId,
            LocalDate weekStartDate) {
        
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con id: " + stationId));

        // Asegurar que sea lunes
        LocalDate weekStart = weekStartDate;
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        // Obtener todas las islas de la estación
        List<Island> islands = islandRepository.findByStationIdAndActiveTrue(stationId);

        // Construir horario para cada isla
        List<WorkerAssignmentDTO.IslandWeeklyScheduleResponse> schedules = new ArrayList<>();
        for (Island island : islands) {
            schedules.add(getIslandWeeklySchedule(island.getId(), weekStart));
        }

        return schedules;
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Reemplazar trabajador en una asignación
     */
    @Transactional
    public WorkerAssignmentDTO.AssignmentResponse replaceWorker(
            Long assignmentId,
            WorkerAssignmentDTO.ReplaceWorkerRequest request) {
        
        WorkerAssignment assignment = workerAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con id: " + assignmentId));

        User newWorker = userRepository.findById(request.newWorkerId())
            .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con id: " + request.newWorkerId()));

        // Verificar que el nuevo usuario tenga rol GRIFERO (con o sin prefijo ROLE_)
        boolean isGrifero = newWorker.getRoles().stream()
            .anyMatch(role -> role.getName().equals("GRIFERO") || role.getName().equals("ROLE_GRIFERO"));
        if (!isGrifero) {
            throw new IllegalArgumentException("El nuevo usuario debe tener rol GRIFERO");
        }

        // Marcar la asignación anterior como REPLACED primero
        // (esto es importante para que las validaciones no la detecten)
        assignment.setStatus(WorkerAssignment.AssignmentStatus.REPLACED);
        assignment.setNotes(
            (assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
            "Reemplazado. Razón: " + request.reason()
        );
        workerAssignmentRepository.save(assignment);

        // Validar que el nuevo trabajador no tenga conflictos
        LocalDate weekStart = assignment.getWeekStartDate();
        DayOfWeek dayOfWeek = assignment.getDayOfWeek();
        
        if (assignment.getRestDay()) {
            // Si es descanso, verificar que el nuevo trabajador no tenga turnos activos ese día
            if (workerAssignmentRepository.hasActiveShiftsForDay(
                    request.newWorkerId(), weekStart, dayOfWeek)) {
                throw new IllegalArgumentException(
                    String.format("El nuevo trabajador ya tiene turnos activos para %s. No se puede asignar descanso.",
                        getDayNameInSpanish(dayOfWeek))
                );
            }
        } else {
            // Si es turno activo, verificar que no tenga descanso ese día
            if (workerAssignmentRepository.hasRestDayAssignment(
                    request.newWorkerId(), weekStart, dayOfWeek)) {
                throw new IllegalArgumentException(
                    String.format("El nuevo trabajador tiene descanso asignado para %s. No se puede asignar turno activo.",
                        getDayNameInSpanish(dayOfWeek))
                );
            }
            
            // Verificar que no exista otro trabajador ACTIVO en la misma isla/turno/día
            if (workerAssignmentRepository.existsActiveAssignmentForIslandShiftDay(
                    assignment.getIsland().getId(), 
                    assignment.getShiftSchedule().getId(), 
                    weekStart, 
                    dayOfWeek)) {
                throw new IllegalArgumentException(
                    String.format("Ya existe otro trabajador asignado a %s en el turno %s para %s",
                        assignment.getIsland().getName(), 
                        assignment.getShiftSchedule().getDisplayLabel(), 
                        getDayNameInSpanish(dayOfWeek))
                );
            }
        }

        // Crear nueva asignación
        WorkerAssignment newAssignment = new WorkerAssignment();
        newAssignment.setWorker(newWorker);
        newAssignment.setIsland(assignment.getIsland());
        newAssignment.setShiftSchedule(assignment.getShiftSchedule());
        newAssignment.setWeekStartDate(assignment.getWeekStartDate());
        newAssignment.setDayOfWeek(assignment.getDayOfWeek());
        newAssignment.setRestDay(assignment.getRestDay());
        newAssignment.setNotes("Reemplazo de: " + assignment.getWorker().getUsername() + ". " + request.reason());
        newAssignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);

        newAssignment = workerAssignmentRepository.save(newAssignment);

        return toAssignmentResponse(newAssignment);
    }

    /**
     * Cancelar asignación
     */
    @Transactional
    public WorkerAssignmentDTO.OperationResponse cancelAssignment(Long id, String reason) {
        WorkerAssignment assignment = workerAssignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con id: " + id));

        assignment.setStatus(WorkerAssignment.AssignmentStatus.CANCELLED);
        assignment.setNotes(
            (assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
            "Cancelado. Razón: " + reason
        );
        workerAssignmentRepository.save(assignment);

        return new WorkerAssignmentDTO.OperationResponse(
            true,
            "Asignación cancelada exitosamente",
            id
        );
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Eliminar asignación
     */
    @Transactional
    public WorkerAssignmentDTO.OperationResponse deleteAssignment(Long id) {
        if (!workerAssignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Asignación no encontrada con id: " + id);
        }

        workerAssignmentRepository.deleteById(id);

        return new WorkerAssignmentDTO.OperationResponse(
            true,
            "Asignación eliminada exitosamente",
            id
        );
    }

    // ==================== HELPER METHODS ====================

    private WorkerAssignmentDTO.AssignmentResponse toAssignmentResponse(WorkerAssignment assignment) {
        return new WorkerAssignmentDTO.AssignmentResponse(
            assignment.getId(),
            assignment.getWorker().getId(),
            assignment.getWorker().getUsername(),
            assignment.getIsland().getId(),
            assignment.getIsland().getName(),
            assignment.getShiftSchedule().getId(),
            assignment.getShiftSchedule().getName(),
            assignment.getShiftSchedule().getDisplayLabel(),
            assignment.getWeekStartDate(),
            assignment.getDayOfWeek(),
            assignment.getRestDay(),
            assignment.getStatus().name(),
            assignment.getNotes(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }

    private WorkerAssignmentDTO.ShiftAssignment toShiftAssignment(WorkerAssignment assignment) {
        return new WorkerAssignmentDTO.ShiftAssignment(
            assignment.getId(),
            assignment.getShiftSchedule().getId(),
            assignment.getShiftSchedule().getName(),
            assignment.getShiftSchedule().getDisplayLabel(),
            assignment.getWorker().getId(),
            assignment.getWorker().getUsername(),
            assignment.getWorker().getFirstName(),
            assignment.getWorker().getLastName(),
            assignment.getRestDay(),
            assignment.getStatus().name()
        );
    }

    private String getDayNameInSpanish(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}
