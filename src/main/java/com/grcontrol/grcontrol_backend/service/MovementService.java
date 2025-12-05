package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.MovementDTO;
import com.grcontrol.grcontrol_backend.entity.Movement;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.repository.MovementRepository;
import com.grcontrol.grcontrol_backend.repository.ShiftSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gestión de movimientos
 */
@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;
    private final ShiftSessionRepository shiftSessionRepository;

    /**
     * Crear nuevo movimiento
     */
    @Transactional
    public MovementDTO.MovementResponse createMovement(MovementDTO.CreateMovementRequest request) {
        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        Movement movement = new Movement();
        movement.setMovementId(UUID.randomUUID().toString());
        movement.setSession(session);
        movement.setPaymentMethod(Movement.PaymentMethod.valueOf(request.paymentMethod()));
        movement.setAmount(request.amount());
        movement.setDescription(request.description());
        movement.setMovementTimestamp(LocalDateTime.now());
        movement.setSyncStatus(Movement.SyncStatus.SYNCED);
        
        // Set VISA-specific fields if provided
        movement.setVisaWorkerName(request.visaWorkerName());
        movement.setVehicleType(request.vehicleType());
        movement.setVehicleBrand(request.vehicleBrand());
        movement.setVehiclePlate(request.vehiclePlate());
        movement.setVehicleColor(request.vehicleColor());

        Movement saved = movementRepository.save(movement);
        return mapToMovementResponse(saved);
    }

    /**
     * Obtener movimiento por ID
     */
    @Transactional(readOnly = true)
    public MovementDTO.MovementResponse getMovementById(Long id) {
        Movement movement = movementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));
        return mapToMovementResponse(movement);
    }

    /**
     * Obtener movimientos por sesión
     */
    @Transactional(readOnly = true)
    public List<MovementDTO.MovementResponse> getMovementsBySession(Long sessionId) {
        return movementRepository.findBySessionId(sessionId).stream()
            .map(this::mapToMovementResponse)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar movimiento
     */
    @Transactional
    public MovementDTO.MovementResponse updateMovement(Long id, MovementDTO.UpdateMovementRequest request) {
        Movement movement = movementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));

        if (request.paymentMethod() != null) {
            movement.setPaymentMethod(Movement.PaymentMethod.valueOf(request.paymentMethod()));
        }
        if (request.amount() != null) {
            movement.setAmount(request.amount());
        }
        if (request.description() != null) {
            movement.setDescription(request.description());
        }
        
        // Update VISA-specific fields if provided
        if (request.visaWorkerName() != null) {
            movement.setVisaWorkerName(request.visaWorkerName());
        }
        if (request.vehicleType() != null) {
            movement.setVehicleType(request.vehicleType());
        }
        if (request.vehicleBrand() != null) {
            movement.setVehicleBrand(request.vehicleBrand());
        }
        if (request.vehiclePlate() != null) {
            movement.setVehiclePlate(request.vehiclePlate());
        }
        if (request.vehicleColor() != null) {
            movement.setVehicleColor(request.vehicleColor());
        }

        Movement saved = movementRepository.save(movement);
        return mapToMovementResponse(saved);
    }

    /**
     * Eliminar movimiento
     */
    @Transactional
    public void deleteMovement(Long id) {
        Movement movement = movementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));
        movementRepository.delete(movement);
    }

    /**
     * Mapear a MovementResponse
     */
    private MovementDTO.MovementResponse mapToMovementResponse(Movement movement) {
        return new MovementDTO.MovementResponse(
            movement.getId(),
            movement.getMovementId(),
            movement.getSession().getId(),
            movement.getPaymentMethod().name(),
            movement.getAmount(),
            movement.getDescription(),
            // VISA fields
            movement.getVisaWorkerName(),
            movement.getVehicleType(),
            movement.getVehicleBrand(),
            movement.getVehiclePlate(),
            movement.getVehicleColor(),
            movement.getMovementTimestamp(),
            movement.getSyncStatus().name(),
            movement.getCreatedAt(),
            movement.getUpdatedAt()
        );
    }
}
