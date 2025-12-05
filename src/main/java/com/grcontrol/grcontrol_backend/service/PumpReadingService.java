package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.PumpReadingDTO;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.PumpReading;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.repository.NozzleRepository;
import com.grcontrol.grcontrol_backend.repository.PumpReadingRepository;
import com.grcontrol.grcontrol_backend.repository.ShiftSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gestión de lecturas de bombas
 */
@Service
@RequiredArgsConstructor
public class PumpReadingService {

    private final PumpReadingRepository pumpReadingRepository;
    private final ShiftSessionRepository shiftSessionRepository;
    private final NozzleRepository nozzleRepository;

    /**
     * Crear nueva lectura
     */
    @Transactional
    public PumpReadingDTO.ReadingResponse createReading(PumpReadingDTO.CreateReadingRequest request) {
        // Validar sesión
        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        // Validar nozzle
        Nozzle nozzle = nozzleRepository.findById(request.nozzleId())
            .orElseThrow(() -> new IllegalArgumentException("Nozzle no encontrado"));

        // Crear lectura
        PumpReading reading = new PumpReading();
        reading.setSession(session);
        reading.setNozzle(nozzle);
        reading.setEntryDigits(request.digits());
        reading.setDifference(request.difference());
        reading.setCompleted(request.completed() != null ? request.completed() : false);
        reading.setReadingTimestamp(LocalDateTime.now());

        // Si hay base reading, establecer relación
        if (request.baseReadingId() != null) {
            PumpReading baseReading = pumpReadingRepository.findById(request.baseReadingId())
                .orElse(null);
            reading.setBaseReading(baseReading);
        }

        // Campos deprecated (para compatibilidad)
        reading.setIslandId(nozzle.getPump().getIsland().getId().intValue());
        reading.setPumpId(nozzle.getPump().getId().intValue());
        reading.setSide(PumpReading.PumpSide.valueOf(nozzle.getSide().name()));
        reading.setNozzleIndex(nozzle.getPosition());
        reading.setFuelType(PumpReading.FuelType.valueOf(nozzle.getFuelType().name()));

        PumpReading saved = pumpReadingRepository.save(reading);
        return mapToReadingResponse(saved);
    }

    /**
     * Crear múltiples lecturas en batch
     */
    @Transactional
    public PumpReadingDTO.BatchResponse createBatchReadings(PumpReadingDTO.BatchCreateRequest request) {
        List<Long> createdIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCreated = 0;
        int totalFailed = 0;

        for (PumpReadingDTO.ReadingData data : request.readings()) {
            try {
                PumpReadingDTO.CreateReadingRequest createRequest = new PumpReadingDTO.CreateReadingRequest(
                    request.sessionId(),
                    data.nozzleId(),
                    data.readingType(),
                    data.digits(),
                    data.difference(),
                    data.completed(),
                    data.baseReadingId()
                );

                PumpReadingDTO.ReadingResponse response = createReading(createRequest);
                createdIds.add(response.id());
                totalCreated++;
            } catch (Exception e) {
                errors.add("Nozzle " + data.nozzleId() + ": " + e.getMessage());
                totalFailed++;
            }
        }

        return new PumpReadingDTO.BatchResponse(
            totalFailed == 0,
            totalCreated,
            totalFailed,
            createdIds,
            errors
        );
    }

    /**
     * Obtener lectura por ID
     */
    @Transactional(readOnly = true)
    public PumpReadingDTO.ReadingResponse getReadingById(Long id) {
        PumpReading reading = pumpReadingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada"));
        return mapToReadingResponse(reading);
    }

    /**
     * Obtener lecturas por sesión
     */
    @Transactional(readOnly = true)
    public List<PumpReadingDTO.ReadingResponse> getReadingsBySession(Long sessionId) {
        return pumpReadingRepository.findBySessionId(sessionId).stream()
            .map(this::mapToReadingResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener lecturas por nozzle
     */
    @Transactional(readOnly = true)
    public List<PumpReadingDTO.ReadingResponse> getReadingsByNozzle(Long nozzleId) {
        return pumpReadingRepository.findByNozzleId(nozzleId).stream()
            .map(this::mapToReadingResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener última lectura completada (para base readings)
     */
    @Transactional(readOnly = true)
    public PumpReadingDTO.BaseReadingResponse getLastCompletedReading(Long nozzleId, String readingType) {
        List<PumpReading> readings = pumpReadingRepository.findLastCompletedByNozzleAndType(
            nozzleId,
            PumpReading.ReadingType.valueOf(readingType)
        );

        if (readings.isEmpty()) {
            throw new IllegalArgumentException("No hay lecturas completadas para este nozzle");
        }

        PumpReading lastReading = readings.get(0); // Ya viene ordenado DESC
        return mapToBaseReadingResponse(lastReading);
    }

    /**
     * Actualizar lectura
     */
    @Transactional
    public PumpReadingDTO.ReadingResponse updateReading(Long id, PumpReadingDTO.UpdateReadingRequest request) {
        PumpReading reading = pumpReadingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada"));

        if (request.digits() != null) {
            reading.setEntryDigits(request.digits());
        }
        if (request.difference() != null) {
            reading.setDifference(request.difference());
        }
        if (request.completed() != null) {
            reading.setCompleted(request.completed());
        }

        PumpReading saved = pumpReadingRepository.save(reading);
        return mapToReadingResponse(saved);
    }

    /**
     * Eliminar lectura (solo si no está completada)
     */
    @Transactional
    public void deleteReading(Long id) {
        PumpReading reading = pumpReadingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lectura no encontrada"));

        if (reading.getCompleted() != null && reading.getCompleted()) {
            throw new IllegalArgumentException("No se pueden eliminar lecturas completadas");
        }

        pumpReadingRepository.delete(reading);
    }

    /**
     * Mapear a ReadingResponse
     */
    private PumpReadingDTO.ReadingResponse mapToReadingResponse(PumpReading reading) {
        Nozzle nozzle = reading.getNozzle();
        return new PumpReadingDTO.ReadingResponse(
            reading.getId(),
            reading.getSession().getId(),
            nozzle.getId(),
            null, // nozzleFuelType
            null, // nozzleSide
            null, // nozzlePosition
            null, // readingType
            null, // digits
            null, // difference
            null, // completed
            null, // baseReadingId
            null, // readingTimestamp
            null, // createdAt
            null  // updatedAt
        );
    }

    /**
     * Mapear a BaseReadingResponse
     * ✨ IMPORTANTE: Devuelve exitDigits del turno anterior para usarse como entrada del siguiente
     */
    private PumpReadingDTO.BaseReadingResponse mapToBaseReadingResponse(PumpReading reading) {
        // Obtener configuración del nozzle para calcular exitValue
        Nozzle nozzle = reading.getNozzle();
        int decimals = getDecimalsForReadingType(nozzle, reading.getReadingType());
        
        // Calcular exitValue desde exitDigits
        double exitValue = calculateValueFromDigits(reading.getExitDigits(), decimals);
        
        return new PumpReadingDTO.BaseReadingResponse(
            reading.getId(),
            nozzle.getId(),
            reading.getSession().getId(),
            reading.getReadingType().name(),
            reading.getExitDigits(),      // ✨ Cambio: devuelve SALIDA del turno anterior
            exitValue,                     // ✨ Nuevo: valor numérico calculado
            reading.getDifference(),
            reading.getReadingTimestamp(),
            reading.getCompleted()
        );
    }
    
    /**
     * Obtener número de decimales según el tipo de lectura y configuración del nozzle
     */
    private int getDecimalsForReadingType(Nozzle nozzle, PumpReading.ReadingType readingType) {
        return switch (readingType) {
            case SOLES -> nozzle.getSolesDecimals();
            case GALLONS -> nozzle.getGallonsDecimals();
            case CLOCK -> nozzle.getClockDecimals();
        };
    }
    
    /**
     * Calcular valor numérico desde string de dígitos
     * Ejemplo: "12345678" con 2 decimales = 123456.78
     */
    private double calculateValueFromDigits(String digitsStr, int decimals) {
        try {
            // Remover corchetes y comillas si es un array JSON
            String cleanStr = digitsStr.replace("[", "")
                                      .replace("]", "")
                                      .replace("\"", "")
                                      .replace(",", "");
            
            // Convertir a número entero
            long digitsAsLong = Long.parseLong(cleanStr);
            
            // Dividir por 10^decimals para obtener el valor real
            return digitsAsLong / Math.pow(10, decimals);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
