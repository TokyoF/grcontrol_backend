package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.ShiftScheduleDTO;
import com.grcontrol.grcontrol_backend.entity.ShiftSchedule;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.repository.ShiftScheduleRepository;
import com.grcontrol.grcontrol_backend.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de horarios de turno
 */
@Service
@RequiredArgsConstructor
public class ShiftScheduleService {

    private final ShiftScheduleRepository shiftScheduleRepository;
    private final StationRepository stationRepository;

    // ==================== CREATE OPERATIONS ====================

    /**
     * Crear nuevo horario de turno
     */
    @Transactional
    public ShiftScheduleDTO.ShiftScheduleResponse createShiftSchedule(ShiftScheduleDTO.ShiftScheduleRequest request) {
        Station station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con id: " + request.stationId()));

        // Validar que no exista un horario con el mismo nombre
        if (shiftScheduleRepository.existsByStationIdAndName(request.stationId(), request.name())) {
            throw new IllegalArgumentException(
                String.format("Ya existe un horario con el nombre '%s' en esta estación", request.name())
            );
        }

        // Validar que no exista un horario con la misma etiqueta
        if (request.displayLabel() != null && 
            shiftScheduleRepository.existsByStationIdAndDisplayLabel(request.stationId(), request.displayLabel())) {
            throw new IllegalArgumentException(
                String.format("Ya existe un horario con la etiqueta '%s' en esta estación", request.displayLabel())
            );
        }

        // Validar horarios
        validateScheduleTimes(request.startTime(), request.endTime());

        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setStation(station);
        schedule.setName(request.name());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setDisplayLabel(request.displayLabel());
        schedule.setActive(request.isActive() != null ? request.isActive() : true);

        schedule = shiftScheduleRepository.save(schedule);

        return toShiftScheduleResponse(schedule);
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Obtener horario por ID
     */
    @Transactional(readOnly = true)
    public ShiftScheduleDTO.ShiftScheduleResponse getShiftSchedule(Long id) {
        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));

        return toShiftScheduleResponse(schedule);
    }

    /**
     * Obtener todos los horarios de una estación
     */
    @Transactional(readOnly = true)
    public ShiftScheduleDTO.StationSchedulesResponse getStationSchedules(Long stationId) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con id: " + stationId));

        List<ShiftSchedule> schedules = shiftScheduleRepository.findByStationId(stationId);

        List<ShiftScheduleDTO.ShiftScheduleResponse> responses = schedules.stream()
            .map(this::toShiftScheduleResponse)
            .collect(Collectors.toList());

        long activeCount = schedules.stream().filter(ShiftSchedule::getActive).count();
        long inactiveCount = schedules.size() - activeCount;

        return new ShiftScheduleDTO.StationSchedulesResponse(
            stationId,
            station.getName(),
            responses,
            (int) activeCount,
            (int) inactiveCount
        );
    }

    /**
     * Obtener horarios activos de una estación
     */
    @Transactional(readOnly = true)
    public List<ShiftScheduleDTO.ShiftScheduleSummary> getActiveSchedules(Long stationId) {
        return shiftScheduleRepository.findByStationIdAndActiveTrue(stationId).stream()
            .map(this::toShiftScheduleSummary)
            .collect(Collectors.toList());
    }

    /**
     * Obtener todos los horarios de todas las estaciones
     */
    @Transactional(readOnly = true)
    public List<ShiftScheduleDTO.ShiftScheduleResponse> getAllSchedules() {
        return shiftScheduleRepository.findAll().stream()
            .map(this::toShiftScheduleResponse)
            .collect(Collectors.toList());
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Actualizar horario de turno
     */
    @Transactional
    public ShiftScheduleDTO.ShiftScheduleResponse updateShiftSchedule(Long id, ShiftScheduleDTO.ShiftScheduleRequest request) {
        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));

        // Validar que no exista otro horario con el mismo nombre
        if (!schedule.getName().equals(request.name()) && 
            shiftScheduleRepository.existsByStationIdAndName(request.stationId(), request.name())) {
            throw new IllegalArgumentException(
                String.format("Ya existe otro horario con el nombre '%s' en esta estación", request.name())
            );
        }

        // Validar horarios
        validateScheduleTimes(request.startTime(), request.endTime());

        schedule.setName(request.name());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setDisplayLabel(request.displayLabel());
        if (request.isActive() != null) {
            schedule.setActive(request.isActive());
        }

        schedule = shiftScheduleRepository.save(schedule);

        return toShiftScheduleResponse(schedule);
    }

    /**
     * Activar/desactivar horario
     */
    @Transactional
    public ShiftScheduleDTO.OperationResponse toggleActive(Long id, ShiftScheduleDTO.ToggleActiveRequest request) {
        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));

        schedule.setActive(request.isActive());
        shiftScheduleRepository.save(schedule);

        String action = request.isActive() ? "activado" : "desactivado";
        return new ShiftScheduleDTO.OperationResponse(
            true,
            String.format("Horario '%s' %s exitosamente", schedule.getName(), action),
            id
        );
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Eliminar horario (solo si no tiene asignaciones)
     */
    @Transactional
    public ShiftScheduleDTO.OperationResponse deleteShiftSchedule(Long id) {
        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));

        // TODO: Verificar que no tenga asignaciones activas
        // Esta validación se puede hacer cuando se implemente WorkerAssignmentService

        shiftScheduleRepository.delete(schedule);

        return new ShiftScheduleDTO.OperationResponse(
            true,
            String.format("Horario '%s' eliminado exitosamente", schedule.getName()),
            id
        );
    }

    // ==================== HELPER METHODS ====================

    /**
     * Validar que los horarios sean válidos
     */
    private void validateScheduleTimes(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Los horarios de inicio y fin son obligatorios");
        }

        // Los horarios pueden cruzar medianoche (ej: 22:00 - 06:00), así que no validamos start < end
        // El validador solo verifica que sean valores válidos
    }

    /**
     * Convertir entidad a response
     */
    private ShiftScheduleDTO.ShiftScheduleResponse toShiftScheduleResponse(ShiftSchedule schedule) {
        return new ShiftScheduleDTO.ShiftScheduleResponse(
            schedule.getId(),
            schedule.getStation().getId(),
            schedule.getStation().getName(),
            schedule.getName(),
            schedule.getDisplayLabel(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            isOvernightShift(schedule.getStartTime(), schedule.getEndTime()),
            schedule.getActive(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }

    /**
     * Convertir entidad a summary
     */
    private ShiftScheduleDTO.ShiftScheduleSummary toShiftScheduleSummary(ShiftSchedule schedule) {
        return new ShiftScheduleDTO.ShiftScheduleSummary(
            schedule.getId(),
            schedule.getName(),
            schedule.getDisplayLabel(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            isOvernightShift(schedule.getStartTime(), schedule.getEndTime())
        );
    }

    /**
     * Determinar si un turno cruza la medianoche
     */
    private Boolean isOvernightShift(LocalTime startTime, LocalTime endTime) {
        return startTime.isAfter(endTime);
    }
}
