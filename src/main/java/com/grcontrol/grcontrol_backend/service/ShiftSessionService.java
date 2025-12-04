package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.ShiftSessionDTO;
import com.grcontrol.grcontrol_backend.entity.*;
import com.grcontrol.grcontrol_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gestión de sesiones de turno (NUEVO FORMATO)
 */
@Service
@RequiredArgsConstructor
public class ShiftSessionService {

    private final ShiftSessionRepository shiftSessionRepository;
    private final UserRepository userRepository;
    private final WorkerAssignmentRepository assignmentRepository;

    /**
     * Crear nueva sesión de turno
     */
    @Transactional
    public ShiftSessionDTO.SessionResponse createSession(ShiftSessionDTO.CreateSessionRequest request) {
        // Validar worker
        User worker = userRepository.findById(request.workerId())
            .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado"));

        // Validar assignment
        WorkerAssignment assignment = assignmentRepository.findById(request.assignmentId())
            .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));

        // Crear sesión
        ShiftSession session = new ShiftSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setOperator(worker);
        session.setAssignment(assignment);
        session.setStation(assignment.getIsland().getStation());
        session.setShiftSchedule(assignment.getShiftSchedule());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(ShiftSession.SessionStatus.ACTIVE);
        session.setConfigurationSnapshot(request.configurationSnapshot());
        session.setTotalSales(0.0);
        session.setSyncStatus(ShiftSession.SyncStatus.SYNCED);

        ShiftSession saved = shiftSessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    /**
     * Obtener sesión por ID
     */
    @Transactional(readOnly = true)
    public ShiftSessionDTO.SessionResponse getSessionById(Long id) {
        ShiftSession session = shiftSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));
        return mapToSessionResponse(session);
    }

    /**
     * Obtener sesión por sessionId (generado por cliente)
     */
    @Transactional(readOnly = true)
    public ShiftSessionDTO.SessionResponse getSessionBySessionId(String sessionId) {
        ShiftSession session = shiftSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));
        return mapToSessionResponse(session);
    }

    /**
     * Obtener sesiones de un trabajador
     */
    @Transactional(readOnly = true)
    public List<ShiftSessionDTO.SessionSummary> getSessionsByWorker(Long workerId) {
        return shiftSessionRepository.findByWorkerId(workerId).stream()
            .map(this::mapToSessionSummary)
            .collect(Collectors.toList());
    }

    /**
     * Obtener sesiones activas
     */
    @Transactional(readOnly = true)
    public List<ShiftSessionDTO.SessionSummary> getActiveSessions() {
        return shiftSessionRepository.findActiveSessions().stream()
            .map(this::mapToSessionSummary)
            .collect(Collectors.toList());
    }

    /**
     * Cerrar sesión
     */
    @Transactional
    public ShiftSessionDTO.SessionResponse closeSession(Long id) {
        ShiftSession session = shiftSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        if (session.getStatus() == ShiftSession.SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("La sesión ya está cerrada");
        }

        session.setStatus(ShiftSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        ShiftSession saved = shiftSessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    /**
     * Pausar sesión
     */
    @Transactional
    public void pauseSession(Long id) {
        ShiftSession session = shiftSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        if (session.getStatus() != ShiftSession.SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Solo se pueden pausar sesiones activas");
        }

        session.setStatus(ShiftSession.SessionStatus.PAUSED);
        shiftSessionRepository.save(session);
    }

    /**
     * Reanudar sesión
     */
    @Transactional
    public void resumeSession(Long id) {
        ShiftSession session = shiftSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        if (session.getStatus() != ShiftSession.SessionStatus.PAUSED) {
            throw new IllegalArgumentException("Solo se pueden reanudar sesiones pausadas");
        }

        session.setStatus(ShiftSession.SessionStatus.ACTIVE);
        shiftSessionRepository.save(session);
    }

    /**
     * Obtener estadísticas de sesión
     */
    @Transactional(readOnly = true)
    public ShiftSessionDTO.SessionStats getSessionStats(Long id) {
        ShiftSession session = shiftSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        int totalReadings = session.getReadings().size();
        int completedReadings = (int) session.getReadings().stream()
            .filter(r -> r.getCompleted() != null && r.getCompleted())
            .count();
        int pendingReadings = totalReadings - completedReadings;

        int movementsCount = session.getMovements().size();
        double movementsTotal = session.getMovements().stream()
            .mapToDouble(Movement::getAmount)
            .sum();

        // Calcular duración en minutos
        LocalDateTime end = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
        long durationMinutes = Duration.between(session.getStartTime(), end).toMinutes();

        return new ShiftSessionDTO.SessionStats(
            session.getId(),
            session.getTotalSales(),
            totalReadings,
            completedReadings,
            pendingReadings,
            movementsCount,
            movementsTotal,
            false, // TODO: implementar hasArqueo cuando exista relación
            0.0,   // TODO: implementar arqueoDifference
            (int) durationMinutes
        );
    }

    /**
     * Mapear a SessionResponse
     */
    private ShiftSessionDTO.SessionResponse mapToSessionResponse(ShiftSession session) {
        return new ShiftSessionDTO.SessionResponse(
            session.getId(),
            session.getSessionId(),
            session.getOperator().getId(),
            session.getOperator().getFirstName() + " " + session.getOperator().getLastName(),
            session.getStation() != null ? session.getStation().getId() : null,
            session.getStation() != null ? session.getStation().getName() : null,
            session.getAssignment() != null ? session.getAssignment().getIsland().getId() : null,
            session.getAssignment() != null ? session.getAssignment().getIsland().getName() : null,
            session.getShiftSchedule() != null ? session.getShiftSchedule().getId() : null,
            session.getShiftSchedule() != null ? session.getShiftSchedule().getName() : null,
            session.getShiftSchedule() != null ? session.getShiftSchedule().getDisplayLabel() : null,
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus().name(),
            session.getTotalSales(),
            session.getReadings().size(),
            session.getMovements().size(),
            false, // TODO: hasArqueo
            session.getConfigurationSnapshot(),
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }

    /**
     * Mapear a SessionSummary
     */
    private ShiftSessionDTO.SessionSummary mapToSessionSummary(ShiftSession session) {
        return new ShiftSessionDTO.SessionSummary(
            session.getId(),
            session.getSessionId(),
            session.getOperator().getFirstName() + " " + session.getOperator().getLastName(),
            session.getStation() != null ? session.getStation().getName() : "N/A",
            session.getAssignment() != null ? session.getAssignment().getIsland().getName() : "N/A",
            session.getShiftSchedule() != null ? session.getShiftSchedule().getDisplayLabel() : "N/A",
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus().name(),
            session.getTotalSales()
        );
    }
}
