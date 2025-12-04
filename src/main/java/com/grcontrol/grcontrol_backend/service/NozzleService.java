package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.NozzleDTO;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.Pump;
import com.grcontrol.grcontrol_backend.entity.PumpConfigurationHistory;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.repository.NozzleRepository;
import com.grcontrol.grcontrol_backend.repository.PumpRepository;
import com.grcontrol.grcontrol_backend.repository.PumpConfigurationHistoryRepository;
import com.grcontrol.grcontrol_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de mangueras con control de precios y auditoría
 */
@Service
@RequiredArgsConstructor
public class NozzleService {

    private final NozzleRepository nozzleRepository;
    private final PumpRepository pumpRepository;
    private final PumpConfigurationHistoryRepository configHistoryRepository;
    private final UserRepository userRepository;

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public NozzleDTO.NozzleResponse createNozzle(NozzleDTO.NozzleRequest request) {
        Pump pump = pumpRepository.findById(request.pumpId())
            .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + request.pumpId()));

        // Validar que no exista otra manguera con mismo lado y tipo de combustible
        Nozzle.PumpSide side = Nozzle.PumpSide.valueOf(request.side());
        Nozzle.FuelType fuelType = Nozzle.FuelType.valueOf(request.fuelType());

        if (nozzleRepository.existsByPumpIdAndSideAndFuelType(request.pumpId(), side, fuelType)) {
            throw new IllegalArgumentException("Nozzle with side " + request.side() +
                " and fuel type " + request.fuelType() + " already exists in this pump");
        }

        Nozzle nozzle = new Nozzle();
        nozzle.setPump(pump);
        nozzle.setSide(side);
        nozzle.setPosition(request.position());
        nozzle.setFuelType(fuelType);
        nozzle.setPricePerGallon(request.pricePerGallon() != null
            ? BigDecimal.valueOf(request.pricePerGallon())
            : null);
        nozzle.setColor(request.color());
        nozzle.setActive(request.isActive() != null ? request.isActive() : true);

        nozzle = nozzleRepository.save(nozzle);

        return toNozzleResponse(nozzle);
    }

    @Transactional(readOnly = true)
    public NozzleDTO.NozzleResponse getNozzle(Long id) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        return toNozzleResponse(nozzle);
    }

    @Transactional(readOnly = true)
    public NozzleDTO.NozzleDetailResponse getNozzleDetail(Long id) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        return toNozzleDetailResponse(nozzle);
    }

    @Transactional(readOnly = true)
    public List<NozzleDTO.NozzleResponse> getNozzlesByPump(Long pumpId) {
        return nozzleRepository.findByPumpIdOrderByPosition(pumpId).stream()
            .map(this::toNozzleResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NozzleDTO.NozzleResponse> getActiveNozzlesByStation(Long stationId) {
        return nozzleRepository.findActiveNozzlesByStationId(stationId).stream()
            .map(this::toNozzleResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public NozzleDTO.NozzleResponse updateNozzle(Long id, NozzleDTO.NozzleRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        // Actualizar pump si cambió
        if (!nozzle.getPump().getId().equals(request.pumpId())) {
            Pump newPump = pumpRepository.findById(request.pumpId())
                .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + request.pumpId()));
            nozzle.setPump(newPump);
        }

        nozzle.setSide(Nozzle.PumpSide.valueOf(request.side()));
        nozzle.setPosition(request.position());
        nozzle.setFuelType(Nozzle.FuelType.valueOf(request.fuelType()));
        if (request.pricePerGallon() != null) {
            nozzle.setPricePerGallon(BigDecimal.valueOf(request.pricePerGallon()));
        }
        nozzle.setColor(request.color());
        if (request.isActive() != null) {
            nozzle.setActive(request.isActive());
        }

        nozzle = nozzleRepository.save(nozzle);

        return toNozzleResponse(nozzle);
    }

    // ==================== PRICE MANAGEMENT WITH AUDIT ====================

    @Transactional
    public NozzleDTO.OperationResponse updatePrice(Long id, NozzleDTO.UpdatePriceRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        BigDecimal oldPrice = nozzle.getPricePerGallon();
        BigDecimal newPrice = BigDecimal.valueOf(request.newPrice());

        // Actualizar precio
        nozzle.setPricePerGallon(newPrice);
        nozzleRepository.save(nozzle);

        // Registrar en historial
        recordConfigurationChange(
            nozzle,
            PumpConfigurationHistory.ConfigChangeType.PRICE_CHANGE,
            oldPrice != null ? oldPrice.toString() : "null",
            newPrice.toString(),
            request.changedById(),
            null,
            request.reason(),
            "Price updated from " + oldPrice + " to " + newPrice
        );

        return new NozzleDTO.OperationResponse(
            true,
            "Price updated successfully",
            id
        );
    }

    @Transactional
    public NozzleDTO.BatchOperationResponse batchUpdatePrices(NozzleDTO.BatchUpdatePricesRequest request) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (var update : request.updates()) {
            try {
                NozzleDTO.UpdatePriceRequest priceRequest = new NozzleDTO.UpdatePriceRequest(
                    update.newPrice(),
                    request.reason(),
                    request.changedById()
                );
                updatePrice(update.nozzleId(), priceRequest);
                successCount++;
            } catch (Exception e) {
                errors.add("Nozzle " + update.nozzleId() + ": " + e.getMessage());
            }
        }

        return new NozzleDTO.BatchOperationResponse(
            errors.isEmpty(),
            successCount + " prices updated, " + errors.size() + " failed",
            successCount,
            errors.size(),
            errors
        );
    }

    @Transactional
    public NozzleDTO.OperationResponse toggleActive(Long id, NozzleDTO.ToggleActiveRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        boolean oldActive = nozzle.getActive();
        nozzle.setActive(request.isActive());
        nozzleRepository.save(nozzle);

        // Registrar en historial
        PumpConfigurationHistory.ConfigChangeType changeType = request.isActive()
            ? PumpConfigurationHistory.ConfigChangeType.ACTIVATION
            : PumpConfigurationHistory.ConfigChangeType.DEACTIVATION;

        recordConfigurationChange(
            nozzle,
            changeType,
            String.valueOf(oldActive),
            String.valueOf(request.isActive()),
            request.changedById(),
            null,
            request.reason(),
            "Nozzle " + (request.isActive() ? "activated" : "deactivated")
        );

        return new NozzleDTO.OperationResponse(
            true,
            "Nozzle " + (request.isActive() ? "activated" : "deactivated") + " successfully",
            id
        );
    }

    @Transactional
    public NozzleDTO.OperationResponse changeFuelType(Long id, NozzleDTO.ChangeFuelTypeRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        String oldFuelType = nozzle.getFuelType().name();
        Nozzle.FuelType newFuelType = Nozzle.FuelType.valueOf(request.newFuelType());

        nozzle.setFuelType(newFuelType);
        nozzleRepository.save(nozzle);

        // Registrar en historial
        recordConfigurationChange(
            nozzle,
            PumpConfigurationHistory.ConfigChangeType.FUEL_TYPE_CHANGE,
            oldFuelType,
            newFuelType.name(),
            request.changedById(),
            null,
            request.reason(),
            "Fuel type changed from " + oldFuelType + " to " + newFuelType
        );

        return new NozzleDTO.OperationResponse(
            true,
            "Fuel type changed successfully",
            id
        );
    }

    @Transactional
    public void deleteNozzle(Long id) {
        if (!nozzleRepository.existsById(id)) {
            throw new IllegalArgumentException("Nozzle not found with id: " + id);
        }
        nozzleRepository.deleteById(id);
    }

    // ==================== HELPERS ====================

    private void recordConfigurationChange(Nozzle nozzle,
                                          PumpConfigurationHistory.ConfigChangeType changeType,
                                          String previousValue,
                                          String newValue,
                                          Long changedById,
                                          Long affectedSessionId,
                                          String reason,
                                          String notes) {
        PumpConfigurationHistory history = new PumpConfigurationHistory();
        history.setNozzle(nozzle);
        history.setChangeType(changeType);
        history.setPreviousValue(previousValue);
        history.setNewValue(newValue);

        if (changedById != null) {
            User changedBy = userRepository.findById(changedById).orElse(null);
            history.setChangedBy(changedBy);
        }

        history.setReason(reason);
        history.setNotes(notes);

        configHistoryRepository.save(history);
    }

    private NozzleDTO.NozzleResponse toNozzleResponse(Nozzle nozzle) {
        return new NozzleDTO.NozzleResponse(
            nozzle.getId(),
            nozzle.getPump().getId(),
            nozzle.getPump().getName(),
            nozzle.getSide().name(),
            nozzle.getPosition(),
            nozzle.getFuelType().name(),
            nozzle.getPricePerGallon() != null ? nozzle.getPricePerGallon().doubleValue() : null,
            nozzle.getColor(),
            nozzle.getActive(),
            nozzle.getCreatedAt(),
            nozzle.getUpdatedAt()
        );
    }

    private NozzleDTO.NozzleDetailResponse toNozzleDetailResponse(Nozzle nozzle) {
        return new NozzleDTO.NozzleDetailResponse(
            nozzle.getId(),
            nozzle.getPump().getId(),
            nozzle.getPump().getName(),
            nozzle.getPump().getIsland().getId(),
            nozzle.getPump().getIsland().getName(),
            nozzle.getPump().getIsland().getStation().getId(),
            nozzle.getPump().getIsland().getStation().getName(),
            nozzle.getSide().name(),
            nozzle.getPosition(),
            nozzle.getFuelType().name(),
            nozzle.getPricePerGallon() != null ? nozzle.getPricePerGallon().doubleValue() : null,
            nozzle.getColor(),
            nozzle.getActive(),
            nozzle.getCreatedAt(),
            nozzle.getUpdatedAt()
        );
    }
}
