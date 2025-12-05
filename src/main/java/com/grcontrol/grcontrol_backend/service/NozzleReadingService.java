package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.NozzleReadingDTO;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.NozzleReading;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.repository.NozzleReadingRepository;
import com.grcontrol.grcontrol_backend.repository.NozzleRepository;
import com.grcontrol.grcontrol_backend.repository.ShiftSessionRepository;
import com.grcontrol.grcontrol_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de lecturas de contómetros
 * Maneja SOLES, GALLONS, CLOCK readings con validación y auditoría
 */
@Service
@RequiredArgsConstructor
public class NozzleReadingService {

    private final NozzleReadingRepository nozzleReadingRepository;
    private final NozzleRepository nozzleRepository;
    private final ShiftSessionRepository shiftSessionRepository;
    private final UserRepository userRepository;

    // ==================== CREATE OPERATIONS ====================

    /**
     * Crear una lectura individual (usado internamente)
     */
    @Transactional
    public NozzleReadingDTO.ReadingResponse createReading(NozzleReadingDTO.ReadingRequest request) {
        Nozzle nozzle = nozzleRepository.findById(request.nozzleId())
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada con id: " + request.nozzleId()));

        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada con id: " + request.sessionId()));

        NozzleReading.ReadingType readingType = NozzleReading.ReadingType.valueOf(request.readingType());

        // Verificar si ya existe
        if (nozzleReadingRepository.existsByNozzleIdAndSessionIdAndReadingType(
                request.nozzleId(), request.sessionId(), readingType)) {
            throw new IllegalArgumentException(
                String.format("Ya existe una lectura de tipo %s para esta manguera en esta sesión", 
                    readingType.name())
            );
        }

        NozzleReading reading = new NozzleReading();
        reading.setNozzle(nozzle);
        reading.setSession(session);
        reading.setReadingType(readingType);
        reading.setInitialValue(request.initialValue());
        reading.setFinalValue(request.finalValue());
        reading.setDecimals(request.decimals());
        reading.setReadingTimestamp(LocalDateTime.now());

        reading = nozzleReadingRepository.save(reading);

        return toReadingResponse(reading);
    }

    /**
     * Crear todas las lecturas de una manguera (SOLES, GALLONS, CLOCK)
     */
    @Transactional
    public List<NozzleReadingDTO.ReadingResponse> createNozzleReadings(
            NozzleReadingDTO.CreateNozzleReadingsRequest request) {
        
        Nozzle nozzle = nozzleRepository.findById(request.nozzleId())
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada con id: " + request.nozzleId()));

        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada con id: " + request.sessionId()));

        List<NozzleReading> readings = new ArrayList<>();

        // Crear lectura de SOLES
        if (request.soles() != null) {
            readings.add(createReadingEntity(nozzle, session, NozzleReading.ReadingType.SOLES, request.soles()));
        }

        // Crear lectura de GALLONS
        if (request.gallons() != null) {
            readings.add(createReadingEntity(nozzle, session, NozzleReading.ReadingType.GALLONS, request.gallons()));
        }

        // Crear lectura de CLOCK
        if (request.clock() != null) {
            readings.add(createReadingEntity(nozzle, session, NozzleReading.ReadingType.CLOCK, request.clock()));
        }

        readings = nozzleReadingRepository.saveAll(readings);

        return readings.stream()
            .map(this::toReadingResponse)
            .collect(Collectors.toList());
    }

    /**
     * Inicializar lecturas para toda una sesión (múltiples mangueras)
     */
    @Transactional
    public NozzleReadingDTO.OperationResponse initializeSessionReadings(
            NozzleReadingDTO.InitializeSessionReadingsRequest request) {
        
        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada con id: " + request.sessionId()));

        int createdCount = 0;
        List<String> errors = new ArrayList<>();

        for (var nozzleData : request.nozzles()) {
            try {
                Nozzle nozzle = nozzleRepository.findById(nozzleData.nozzleId())
                    .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada: " + nozzleData.nozzleId()));

                // Obtener configuración de decimales de la manguera
                int solesDecimals = nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2;
                int gallonsDecimals = nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3;
                int clockDecimals = nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0;

                // Crear lecturas iniciales
                createOrUpdateReading(nozzle, session, NozzleReading.ReadingType.SOLES, 
                    nozzleData.solesInitial(), null, solesDecimals);
                createOrUpdateReading(nozzle, session, NozzleReading.ReadingType.GALLONS, 
                    nozzleData.gallonsInitial(), null, gallonsDecimals);
                createOrUpdateReading(nozzle, session, NozzleReading.ReadingType.CLOCK, 
                    nozzleData.clockInitial(), null, clockDecimals);

                createdCount++;
            } catch (Exception e) {
                errors.add("Manguera " + nozzleData.nozzleId() + ": " + e.getMessage());
            }
        }

        String message = String.format("Lecturas inicializadas: %d exitosas", createdCount);
        if (!errors.isEmpty()) {
            message += String.format(", %d errores", errors.size());
        }

        return new NozzleReadingDTO.OperationResponse(
            errors.isEmpty(),
            message,
            null
        );
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Actualizar valor final de una lectura (usado por GRIFERO al terminar turno)
     */
    @Transactional
    public NozzleReadingDTO.ReadingResponse updateFinalValue(
            Long readingId, 
            NozzleReadingDTO.UpdateFinalValueRequest request) {
        
        NozzleReading reading = nozzleReadingRepository.findById(readingId)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada con id: " + readingId));

        if (reading.getCompleted()) {
            throw new IllegalArgumentException("Esta lectura ya está completada. Use la modificación administrativa para cambiarla.");
        }

        if (request.finalValue().compareTo(reading.getInitialValue()) < 0) {
            throw new IllegalArgumentException(
                "El valor final no puede ser menor que el valor inicial (" + reading.getInitialValue() + ")"
            );
        }

        reading.setFinalValueAndComplete(request.finalValue());
        reading = nozzleReadingRepository.save(reading);

        return toReadingResponse(reading);
    }

    /**
     * Modificar lectura (solo ADMIN/GERENTE) con auditoría
     */
    @Transactional
    public NozzleReadingDTO.ReadingResponse modifyReading(
            Long readingId,
            NozzleReadingDTO.ModifyReadingRequest request,
            Long modifiedByUserId) {
        
        NozzleReading reading = nozzleReadingRepository.findById(readingId)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada con id: " + readingId));

        User modifiedBy = userRepository.findById(modifiedByUserId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + modifiedByUserId));

        // Validaciones
        if (request.finalValue().compareTo(request.initialValue()) < 0) {
            throw new IllegalArgumentException(
                "El valor final no puede ser menor que el valor inicial"
            );
        }

        if (request.reason() == null || request.reason().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar una razón para la modificación");
        }

        // Actualizar valores
        reading.setInitialValue(request.initialValue());
        reading.setFinalValue(request.finalValue());
        reading.calculateDifference();

        // Marcar como modificada
        reading.setWasModified(true);
        reading.setModifiedBy(modifiedBy);
        reading.setModifiedAt(LocalDateTime.now());
        reading.setModificationReason(request.reason());

        reading = nozzleReadingRepository.save(reading);

        return toReadingResponse(reading);
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Obtener lectura específica por ID
     */
    @Transactional(readOnly = true)
    public NozzleReadingDTO.ReadingResponse getReading(Long id) {
        NozzleReading reading = nozzleReadingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada con id: " + id));

        return toReadingResponse(reading);
    }

    /**
     * Obtener lecturas de una manguera específica en una sesión
     */
    @Transactional(readOnly = true)
    public NozzleReadingDTO.NozzleReadingsResponse getNozzleReadingsBySession(Long nozzleId, Long sessionId) {
        Nozzle nozzle = nozzleRepository.findById(nozzleId)
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada con id: " + nozzleId));

        List<NozzleReading> readings = nozzleReadingRepository.findByNozzleIdAndSessionId(nozzleId, sessionId);

        return toNozzleReadingsResponse(nozzle, readings);
    }

    /**
     * Obtener todas las lecturas de una sesión agrupadas por manguera
     */
    @Transactional(readOnly = true)
    public List<NozzleReadingDTO.NozzleReadingsResponse> getAllReadingsBySession(Long sessionId) {
        List<NozzleReading> allReadings = nozzleReadingRepository.findAllBySessionIdWithNozzleAndPump(sessionId);

        // Agrupar por manguera
        Map<Long, List<NozzleReading>> readingsByNozzle = allReadings.stream()
            .collect(Collectors.groupingBy(r -> r.getNozzle().getId()));

        // Convertir a response
        return readingsByNozzle.entrySet().stream()
            .map(entry -> {
                Long nozzleId = entry.getKey();
                List<NozzleReading> readings = entry.getValue();
                Nozzle nozzle = readings.get(0).getNozzle();
                return toNozzleReadingsResponse(nozzle, readings);
            })
            .collect(Collectors.toList());
    }

    /**
     * Obtener resumen de lecturas de una sesión
     */
    @Transactional(readOnly = true)
    public NozzleReadingDTO.SessionReadingsSummary getSessionReadingsSummary(Long sessionId) {
        ShiftSession session = shiftSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada con id: " + sessionId));

        List<NozzleReading> allReadings = nozzleReadingRepository.findAllBySessionIdWithNozzleAndPump(sessionId);

        // Agrupar por manguera
        Map<Long, List<NozzleReading>> readingsByNozzle = allReadings.stream()
            .collect(Collectors.groupingBy(r -> r.getNozzle().getId()));

        int totalNozzles = readingsByNozzle.size();
        
        // Contar lecturas completas e incompletas
        long completedReadings = allReadings.stream()
            .filter(NozzleReading::getCompleted)
            .count();
        
        long incompleteReadings = allReadings.stream()
            .filter(r -> !r.getCompleted())
            .count();
        
        long modifiedReadings = allReadings.stream()
            .filter(NozzleReading::getWasModified)
            .count();

        // Construir response de mangueras
        List<NozzleReadingDTO.NozzleReadingsResponse> nozzleReadings = readingsByNozzle.entrySet().stream()
            .map(entry -> {
                Nozzle nozzle = entry.getValue().get(0).getNozzle();
                return toNozzleReadingsResponse(nozzle, entry.getValue());
            })
            .collect(Collectors.toList());

        return new NozzleReadingDTO.SessionReadingsSummary(
            sessionId,
            session.getOperator() != null ? session.getOperator().getUsername() : "N/A",
            session.getStartTime(),
            totalNozzles,
            (int) completedReadings,
            (int) incompleteReadings,
            (int) modifiedReadings,
            nozzleReadings
        );
    }

    /**
     * Obtener lecturas incompletas de una sesión
     */
    @Transactional(readOnly = true)
    public List<NozzleReadingDTO.ReadingResponse> getIncompleteReadings(Long sessionId) {
        return nozzleReadingRepository.findIncompleteReadings(sessionId).stream()
            .map(this::toReadingResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener lecturas modificadas de una sesión
     */
    @Transactional(readOnly = true)
    public List<NozzleReadingDTO.ReadingResponse> getModifiedReadings(Long sessionId) {
        return nozzleReadingRepository.findModifiedReadings(sessionId).stream()
            .map(this::toReadingResponse)
            .collect(Collectors.toList());
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Eliminar lectura (solo si no está completada)
     */
    @Transactional
    public void deleteReading(Long id) {
        NozzleReading reading = nozzleReadingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada con id: " + id));

        if (reading.getCompleted()) {
            throw new IllegalArgumentException("No se puede eliminar una lectura completada. Use modificación administrativa.");
        }

        nozzleReadingRepository.delete(reading);
    }

    /**
     * Eliminar todas las lecturas de una sesión (uso administrativo)
     */
    @Transactional
    public NozzleReadingDTO.OperationResponse deleteSessionReadings(Long sessionId) {
        List<NozzleReading> readings = nozzleReadingRepository.findAllBySessionId(sessionId);
        
        if (readings.isEmpty()) {
            return new NozzleReadingDTO.OperationResponse(
                false,
                "No se encontraron lecturas para esta sesión",
                null
            );
        }

        nozzleReadingRepository.deleteBySessionId(sessionId);

        return new NozzleReadingDTO.OperationResponse(
            true,
            String.format("Se eliminaron %d lecturas de la sesión", readings.size()),
            sessionId
        );
    }

    // ==================== HELPER METHODS ====================

    private NozzleReading createReadingEntity(
            Nozzle nozzle,
            ShiftSession session,
            NozzleReading.ReadingType readingType,
            NozzleReadingDTO.ReadingValues values) {
        
        NozzleReading reading = new NozzleReading();
        reading.setNozzle(nozzle);
        reading.setSession(session);
        reading.setReadingType(readingType);
        reading.setInitialValue(values.initialValue());
        reading.setFinalValue(values.finalValue());
        reading.setDecimals(values.decimals());
        reading.setReadingTimestamp(LocalDateTime.now());

        return reading;
    }

    private void createOrUpdateReading(
            Nozzle nozzle,
            ShiftSession session,
            NozzleReading.ReadingType readingType,
            BigDecimal initialValue,
            BigDecimal finalValue,
            Integer decimals) {
        
        Optional<NozzleReading> existing = nozzleReadingRepository
            .findByNozzleIdAndSessionIdAndReadingType(nozzle.getId(), session.getId(), readingType);

        NozzleReading reading;
        if (existing.isPresent()) {
            reading = existing.get();
            reading.setInitialValue(initialValue);
            if (finalValue != null) {
                reading.setFinalValue(finalValue);
            }
        } else {
            reading = new NozzleReading();
            reading.setNozzle(nozzle);
            reading.setSession(session);
            reading.setReadingType(readingType);
            reading.setInitialValue(initialValue);
            reading.setFinalValue(finalValue);
            reading.setDecimals(decimals);
            reading.setReadingTimestamp(LocalDateTime.now());
        }

        nozzleReadingRepository.save(reading);
    }

    private NozzleReadingDTO.ReadingResponse toReadingResponse(NozzleReading reading) {
        return new NozzleReadingDTO.ReadingResponse(
            reading.getId(),
            reading.getNozzle().getId(),
            reading.getNozzle().getFuelType().name(),
            reading.getNozzle().getSide().name(),
            reading.getSession().getId(),
            reading.getReadingType().name(),
            reading.getInitialValue(),
            reading.getFinalValue(),
            reading.getDifference(),
            reading.getDecimals(),
            reading.getCompleted(),
            reading.getWasModified(),
            reading.getModifiedBy() != null ? reading.getModifiedBy().getUsername() : null,
            reading.getModifiedAt(),
            reading.getModificationReason(),
            reading.getReadingTimestamp(),
            reading.getCreatedAt(),
            reading.getUpdatedAt()
        );
    }

    private NozzleReadingDTO.NozzleReadingsResponse toNozzleReadingsResponse(
            Nozzle nozzle, 
            List<NozzleReading> readings) {
        
        Map<NozzleReading.ReadingType, NozzleReading> readingMap = readings.stream()
            .collect(Collectors.toMap(
                NozzleReading::getReadingType,
                r -> r
            ));

        return new NozzleReadingDTO.NozzleReadingsResponse(
            nozzle.getId(),
            nozzle.getFuelType().name(),
            nozzle.getSide().name(),
            nozzle.getPosition(),
            nozzle.getColor(),
            nozzle.getPump().getId(),
            nozzle.getPump().getName(),
            toReadingDetail(readingMap.get(NozzleReading.ReadingType.SOLES)),
            toReadingDetail(readingMap.get(NozzleReading.ReadingType.GALLONS)),
            toReadingDetail(readingMap.get(NozzleReading.ReadingType.CLOCK))
        );
    }

    private NozzleReadingDTO.ReadingDetail toReadingDetail(NozzleReading reading) {
        if (reading == null) {
            return null;
        }

        return new NozzleReadingDTO.ReadingDetail(
            reading.getId(),
            reading.getInitialValue(),
            reading.getFinalValue(),
            reading.getDifference(),
            reading.getDecimals(),
            reading.getCompleted(),
            reading.getWasModified()
        );
    }
}
