package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.ChangeStatusRequest;
import com.grcontrol.grcontrol_backend.dto.WorkerAssignmentDTO;
import com.grcontrol.grcontrol_backend.entity.*;
import com.grcontrol.grcontrol_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de asignaciones de trabajadores y horarios
 */
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final WorkerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final IslandRepository islandRepository;
    private final ShiftScheduleRepository scheduleRepository;

    // ==================== ASSIGNMENT CREATION ====================

    @Transactional
    public WorkerAssignmentDTO.AssignmentResponse createAssignment(WorkerAssignmentDTO.AssignmentRequest request) {
        User worker = userRepository.findById(request.workerId())
            .orElseThrow(() -> new IllegalArgumentException("Worker not found with id: " + request.workerId()));

        Island island = islandRepository.findById(request.islandId())
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + request.islandId()));

        ShiftSchedule schedule = scheduleRepository.findById(request.shiftScheduleId())
            .orElseThrow(() -> new IllegalArgumentException("Shift schedule not found with id: " + request.shiftScheduleId()));

        // Validar que no exista asignación activa para el mismo trabajador, semana y día
        if (assignmentRepository.existsActiveAssignment(
            request.workerId(),
            request.weekStartDate(),
            request.dayOfWeek()
        )) {
            throw new IllegalArgumentException("Worker already has an active assignment for this day");
        }

        WorkerAssignment assignment = new WorkerAssignment();
        assignment.setWorker(worker);
        assignment.setIsland(island);
        assignment.setShiftSchedule(schedule);
        assignment.setWeekStartDate(request.weekStartDate());
        assignment.setDayOfWeek(request.dayOfWeek());
        assignment.setRestDay(request.isRestDay() != null ? request.isRestDay() : false);
        assignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);
        assignment.setNotes(request.notes());

        assignment = assignmentRepository.save(assignment);

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public WorkerAssignmentDTO.BatchOperationResponse createWeeklyAssignment(
        WorkerAssignmentDTO.WeeklyAssignmentRequest request) {

        List<Long> assignmentIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (var dayAssignment : request.assignments()) {
            try {
                WorkerAssignmentDTO.AssignmentRequest assignmentRequest =
                    new WorkerAssignmentDTO.AssignmentRequest(
                        request.workerId(),
                        dayAssignment.islandId(),
                        dayAssignment.shiftScheduleId(),
                        request.weekStartDate(),
                        dayAssignment.dayOfWeek(),
                        dayAssignment.isRestDay(),
                        null
                    );

                var response = createAssignment(assignmentRequest);
                assignmentIds.add(response.id());
            } catch (Exception e) {
                errors.add(dayAssignment.dayOfWeek() + ": " + e.getMessage());
            }
        }

        return new WorkerAssignmentDTO.BatchOperationResponse(
            errors.isEmpty(),
            assignmentIds.size() + " assignments created",
            assignmentIds.size(),
            assignmentIds,
            errors
        );
    }

    // ==================== QUERY OPERATIONS ====================

    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.AssignmentResponse getAssignment(Long id) {
        WorkerAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found with id: " + id));

        return toAssignmentResponse(assignment);
    }

    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.ActiveAssignmentResponse findActiveAssignment(
        WorkerAssignmentDTO.FindActiveAssignmentRequest request) {

        DayOfWeek dayOfWeek = request.date().getDayOfWeek();

        WorkerAssignment assignment = assignmentRepository.findActiveAssignment(
            request.workerId(),
            request.date(),
            dayOfWeek
        ).orElse(null);

        if (assignment == null) {
            return null;
        }

        return toActiveAssignmentResponse(assignment, request.date());
    }

    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.WeeklyAssignmentResponse getWorkerWeeklyAssignments(
        Long workerId, LocalDate weekStartDate) {

        User worker = userRepository.findById(workerId)
            .orElseThrow(() -> new IllegalArgumentException("Worker not found with id: " + workerId));

        List<WorkerAssignment> assignments = assignmentRepository.findByWorkerIdAndWeekStartDate(
            workerId, weekStartDate);

        List<WorkerAssignmentDTO.AssignmentResponse> assignmentResponses = assignments.stream()
            .map(this::toAssignmentResponse)
            .collect(Collectors.toList());

        long restDaysCount = assignments.stream()
            .filter(WorkerAssignment::getRestDay)
            .count();

        return new WorkerAssignmentDTO.WeeklyAssignmentResponse(
            workerId,
            worker.getFirstName() + " " + worker.getLastName(),
            weekStartDate,
            weekStartDate.plusDays(6),
            assignmentResponses,
            (int) restDaysCount,
            assignments.size() - (int) restDaysCount
        );
    }

    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.IslandWeeklyScheduleResponse getIslandWeeklySchedule(
        Long islandId, LocalDate weekStartDate) {

        Island island = islandRepository.findById(islandId)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + islandId));

        List<WorkerAssignment> assignments = assignmentRepository.findByIslandIdAndWeekStartDate(
            islandId, weekStartDate);

        // Agrupar por día
        List<WorkerAssignmentDTO.DaySchedule> schedule = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            List<WorkerAssignment> dayAssignments = assignments.stream()
                .filter(a -> a.getDayOfWeek() == day)
                .collect(Collectors.toList());

            List<WorkerAssignmentDTO.ShiftAssignment> shifts = dayAssignments.stream()
                .map(a -> new WorkerAssignmentDTO.ShiftAssignment(
                    a.getId(),
                    a.getShiftSchedule().getId(),
                    a.getShiftSchedule().getName(),
                    a.getShiftSchedule().getDisplayLabel(),
                    a.getWorker().getId(),
                    a.getWorker().getFirstName() + " " + a.getWorker().getLastName(),
                    a.getRestDay(),
                    a.getStatus().name()
                ))
                .collect(Collectors.toList());

            schedule.add(new WorkerAssignmentDTO.DaySchedule(day, shifts));
        }

        return new WorkerAssignmentDTO.IslandWeeklyScheduleResponse(
            islandId,
            island.getName(),
            weekStartDate,
            weekStartDate.plusDays(6),
            schedule
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerAssignmentDTO.AssignmentResponse> getActiveAssignmentsByWorker(Long workerId) {
        return assignmentRepository.findActiveByWorkerId(workerId).stream()
            .map(this::toAssignmentResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkerAssignmentDTO.WorkerAssignmentStatsResponse getWorkerStats(Long workerId) {
        User worker = userRepository.findById(workerId)
            .orElseThrow(() -> new IllegalArgumentException("Worker not found with id: " + workerId));

        List<WorkerAssignment> allAssignments = assignmentRepository.findByWorkerIdOrderByWeek(workerId);
        long activeCount = assignmentRepository.countByWorkerIdAndStatus(
            workerId, WorkerAssignment.AssignmentStatus.ACTIVE);

        // Calcular días de descanso este mes
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        long restDaysThisMonth = allAssignments.stream()
            .filter(a -> a.getRestDay() &&
                !a.getWeekStartDate().isBefore(monthStart) &&
                !a.getWeekStartDate().isAfter(monthEnd))
            .count();

        long workDaysThisMonth = allAssignments.stream()
            .filter(a -> !a.getRestDay() &&
                !a.getWeekStartDate().isBefore(monthStart) &&
                !a.getWeekStartDate().isAfter(monthEnd))
            .count();

        // Islas asignadas
        List<String> assignedIslands = allAssignments.stream()
            .map(a -> a.getIsland().getName())
            .distinct()
            .collect(Collectors.toList());

        return new WorkerAssignmentDTO.WorkerAssignmentStatsResponse(
            workerId,
            worker.getFirstName() + " " + worker.getLastName(),
            allAssignments.size(),
            (int) activeCount,
            (int) restDaysThisMonth,
            (int) workDaysThisMonth,
            assignedIslands
        );
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public WorkerAssignmentDTO.OperationResponse changeStatus(Long id,
                                                              ChangeStatusRequest request) {
        WorkerAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found with id: " + id));

        WorkerAssignment.AssignmentStatus newStatus = WorkerAssignment.AssignmentStatus.valueOf(request.status());
        assignment.setStatus(newStatus);

        if (request.reason() != null && !request.reason().isEmpty()) {
            String currentNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            assignment.setNotes(currentNotes + "\nStatus change: " + request.reason());
        }

        assignmentRepository.save(assignment);

        return new WorkerAssignmentDTO.OperationResponse(
            true,
            "Assignment status changed to " + newStatus,
            id
        );
    }

    @Transactional
    public WorkerAssignmentDTO.OperationResponse replaceWorker(Long id,
                                                               WorkerAssignmentDTO.ReplaceWorkerRequest request) {
        WorkerAssignment oldAssignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found with id: " + id));

        User newWorker = userRepository.findById(request.newWorkerId())
            .orElseThrow(() -> new IllegalArgumentException("Worker not found with id: " + request.newWorkerId()));

        // Marcar asignación antigua como reemplazada
        oldAssignment.setStatus(WorkerAssignment.AssignmentStatus.REPLACED);
        oldAssignment.setNotes((oldAssignment.getNotes() != null ? oldAssignment.getNotes() : "") +
            "\nReplaced by worker: " + newWorker.getUsername() + ". Reason: " + request.reason());
        assignmentRepository.save(oldAssignment);

        // Crear nueva asignación
        WorkerAssignment newAssignment = new WorkerAssignment();
        newAssignment.setWorker(newWorker);
        newAssignment.setIsland(oldAssignment.getIsland());
        newAssignment.setShiftSchedule(oldAssignment.getShiftSchedule());
        newAssignment.setWeekStartDate(oldAssignment.getWeekStartDate());
        newAssignment.setDayOfWeek(oldAssignment.getDayOfWeek());
        newAssignment.setRestDay(oldAssignment.getRestDay());
        newAssignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);
        newAssignment.setNotes("Replacement for assignment #" + id + ". Reason: " + request.reason());

        newAssignment = assignmentRepository.save(newAssignment);

        return new WorkerAssignmentDTO.OperationResponse(
            true,
            "Worker replaced successfully",
            newAssignment.getId()
        );
    }

    // ==================== HELPERS ====================

    private WorkerAssignmentDTO.AssignmentResponse toAssignmentResponse(WorkerAssignment assignment) {
        return new WorkerAssignmentDTO.AssignmentResponse(
            assignment.getId(),
            assignment.getWorker().getId(),
            assignment.getWorker().getFirstName() + " " + assignment.getWorker().getLastName(),
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

    private WorkerAssignmentDTO.ActiveAssignmentResponse toActiveAssignmentResponse(
        WorkerAssignment assignment, LocalDate date) {
        return new WorkerAssignmentDTO.ActiveAssignmentResponse(
            assignment.getId(),
            assignment.getWorker().getId(),
            assignment.getWorker().getFirstName() + " " + assignment.getWorker().getLastName(),
            assignment.getIsland().getId(),
            assignment.getIsland().getName(),
            assignment.getIsland().getStation().getId(),
            assignment.getIsland().getStation().getName(),
            assignment.getShiftSchedule().getId(),
            assignment.getShiftSchedule().getName(),
            assignment.getShiftSchedule().getDisplayLabel(),
            date,
            assignment.getDayOfWeek(),
            assignment.getRestDay()
        );
    }
}
