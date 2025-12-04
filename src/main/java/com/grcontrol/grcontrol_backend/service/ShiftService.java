package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.ShiftDTO;
import com.grcontrol.grcontrol_backend.entity.*;
import com.grcontrol.grcontrol_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio principal para gestión de turnos y sincronización
 * Implementa toda la lógica de negocio de manera transaccional
 */
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftSessionRepository sessionRepository;
    private final PumpReadingRepository readingRepository;
    private final MovementRepository movementRepository;
    private final ArqueoRepository arqueoRepository;
    private final UserRepository userRepository;

    // ==================== SESSION MANAGEMENT ====================

    @Transactional
    public ShiftDTO.SessionResponse createSession(ShiftDTO.SessionRequest request) {
        // Validar si ya existe
        if (sessionRepository.existsBySessionId(request.sessionId())) {
            throw new IllegalArgumentException("Session ID already exists: " + request.sessionId());
        }

        // Buscar operador por username
        User operator = userRepository.findByUsername(request.operatorId())
            .orElseThrow(() -> new IllegalArgumentException("Operator not found: " + request.operatorId()));

        // Crear sesión
        ShiftSession session = new ShiftSession();
        session.setSessionId(request.sessionId());
        session.setOperator(operator);
        session.setShiftTime(request.shiftTime());
        session.setStartTime(request.startTime());
        session.setStatus(ShiftSession.SessionStatus.ACTIVE);
        session.setStationId(request.stationId());
        session.setStationName(request.stationName());
        session.setSyncStatus(ShiftSession.SyncStatus.SYNCED);

        session = sessionRepository.save(session);

        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public ShiftDTO.SessionResponse getSession(String sessionId) {
        ShiftSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        return toSessionResponse(session);
    }

    @Transactional
    public ShiftDTO.SessionResponse closeSession(ShiftDTO.CloseSessionRequest request) {
        ShiftSession session = sessionRepository.findBySessionId(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.setStatus(ShiftSession.SessionStatus.COMPLETED);
        session.setEndTime(request.endTime());

        // Recalcular totales
        Double totalSales = readingRepository.sumSalesBySession(session);
        session.setTotalSales(totalSales != null ? totalSales : 0.0);

        session = sessionRepository.save(session);

        return toSessionResponse(session);
    }

    // ==================== SYNC OPERATIONS ====================

    @Transactional
    public ShiftDTO.OperationResponse syncBatch(ShiftDTO.SyncBatchRequest request) {
        try {
            // Buscar o crear sesión
            ShiftSession session = sessionRepository.findBySessionId(request.sessionId())
                .orElseGet(() -> {
                    // Auto-crear sesión si viene del móvil
                    ShiftSession newSession = new ShiftSession();
                    newSession.setSessionId(request.sessionId());
                    // Usar primer usuario disponible (mejorar con autenticación)
                    User operator = userRepository.findAll().get(0);
                    newSession.setOperator(operator);
                    newSession.setShiftTime("6-14"); // Default
                    newSession.setStartTime(java.time.LocalDateTime.now());
                    newSession.setStatus(ShiftSession.SessionStatus.ACTIVE);
                    return sessionRepository.save(newSession);
                });

            // Sincronizar lecturas
            if (request.readings() != null) {
                for (var readingReq : request.readings()) {
                    syncReading(session, readingReq);
                }
            }

            // Sincronizar movimientos
            if (request.movements() != null) {
                for (var movementReq : request.movements()) {
                    syncMovement(session, movementReq);
                }
            }

            // Sincronizar arqueo
            if (request.arqueo() != null) {
                syncArqueo(session, request.arqueo());
            }

            // Actualizar total de ventas
            Double totalSales = readingRepository.sumSalesBySession(session);
            session.setTotalSales(totalSales != null ? totalSales : 0.0);
            sessionRepository.save(session);

            return new ShiftDTO.OperationResponse(
                true,
                "Batch synced successfully",
                session.getId()
            );

        } catch (Exception e) {
            return new ShiftDTO.OperationResponse(
                false,
                "Sync failed: " + e.getMessage(),
                null
            );
        }
    }

    private void syncReading(ShiftSession session, ShiftDTO.ReadingRequest req) {
        PumpReading reading = new PumpReading();
        reading.setSession(session);
        reading.setIslandId(req.islandId());
        reading.setIslandName(req.islandName());
        reading.setPumpId(req.pumpId());
        reading.setPumpName(req.pumpName());
        reading.setSide(PumpReading.PumpSide.valueOf(req.side().toUpperCase()));
        reading.setNozzleIndex(req.nozzleIndex());
        reading.setFuelType(PumpReading.FuelType.valueOf(req.fuelType().toUpperCase()));
        reading.setFuelName(req.fuelName());
        reading.setReadingType(PumpReading.ReadingType.valueOf(req.readingType().toUpperCase()));
        reading.setEntryDigits(req.entryDigits());
        reading.setExitDigits(req.exitDigits());
        reading.setDifference(req.difference());
        reading.setCompleted(req.completed());
        reading.setReadingTimestamp(req.readingTimestamp());
        reading.setSyncStatus(PumpReading.SyncStatus.SYNCED);

        readingRepository.save(reading);
    }

    private void syncMovement(ShiftSession session, ShiftDTO.MovementRequest req) {
        // Verificar si ya existe
        if (req.movementId() != null && movementRepository.findByMovementId(req.movementId()).isPresent()) {
            return; // Ya existe, skip
        }

        Movement movement = new Movement();
        movement.setMovementId(req.movementId());
        movement.setSession(session);
        movement.setPaymentMethod(Movement.PaymentMethod.valueOf(req.paymentMethod().toUpperCase()));
        movement.setAmount(req.amount());
        movement.setDescription(req.description());
        movement.setMovementTimestamp(req.movementTimestamp());
        movement.setSyncStatus(Movement.SyncStatus.SYNCED);

        movementRepository.save(movement);
    }

    private void syncArqueo(ShiftSession session, ShiftDTO.ArqueoRequest req) {
        // Verificar si ya existe
        if (arqueoRepository.existsBySession(session)) {
            return; // Ya existe, skip
        }

        Arqueo arqueo = new Arqueo();
        arqueo.setArqueoId(req.arqueoId());
        arqueo.setSession(session);
        arqueo.setEfectivo(req.efectivo());
        arqueo.setTarjetaCredito(req.tarjetaCredito());
        arqueo.setTarjetaDebito(req.tarjetaDebito());
        arqueo.setValeInterno(req.valeInterno());
        arqueo.setDeposito(req.deposito());
        arqueo.setTotalCash(req.totalCash());
        arqueo.setTotalSales(req.totalSales());
        arqueo.setDifference(req.difference());
        arqueo.setStatus(Arqueo.ArqueoStatus.valueOf(req.status().toUpperCase()));
        arqueo.setNotes(req.notes());
        arqueo.setArqueoTimestamp(req.arqueoTimestamp());
        arqueo.setSyncStatus(Arqueo.SyncStatus.SYNCED);

        arqueoRepository.save(arqueo);
    }

    // ==================== STATISTICS ====================

    @Transactional(readOnly = true)
    public ShiftDTO.StatsResponse getSessionStats(String sessionId) {
        ShiftSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        Double totalSales = readingRepository.sumSalesBySession(session);

        // Ventas por combustible
        Double regular = readingRepository.sumSalesBySessionAndFuelType(
            session, PumpReading.FuelType.REGULAR);
        Double premium = readingRepository.sumSalesBySessionAndFuelType(
            session, PumpReading.FuelType.PREMIUM);
        Double diesel = readingRepository.sumSalesBySessionAndFuelType(
            session, PumpReading.FuelType.DIESEL);
        Double glp = readingRepository.sumSalesBySessionAndFuelType(
            session, PumpReading.FuelType.GLP);

        var salesByFuel = new ShiftDTO.SalesByFuel(
            regular != null ? regular : 0.0,
            premium != null ? premium : 0.0,
            diesel != null ? diesel : 0.0,
            glp != null ? glp : 0.0
        );

        List<PumpReading> readings = readingRepository.findBySessionOrderByCreatedAt(session);
        long completedReadings = readings.stream().filter(PumpReading::getCompleted).count();

        Double totalMovements = movementRepository.sumAmountBySession(session);

        return new ShiftDTO.StatsResponse(
            totalSales != null ? totalSales : 0.0,
            salesByFuel,
            readings.size(),
            (int) completedReadings,
            movementRepository.findBySessionOrderByMovementTimestampDesc(session).size(),
            totalMovements != null ? totalMovements : 0.0
        );
    }

    // ==================== HELPERS ====================

    private ShiftDTO.SessionResponse toSessionResponse(ShiftSession session) {
        return new ShiftDTO.SessionResponse(
            session.getId(),
            session.getSessionId(),
            session.getOperator().getFirstName() + " " + session.getOperator().getLastName(),
            session.getShiftTime(),
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus().name(),
            session.getTotalSales(),
            session.getStationId(),
            session.getStationName(),
            session.getReadings().size(),
            session.getMovements().size(),
            session.getCreatedAt()
        );
    }
}
