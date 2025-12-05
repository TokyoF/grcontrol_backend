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
            String sideText = side == Nozzle.PumpSide.LEFT ? "izquierdo" : "derecho";
            throw new IllegalArgumentException(
                String.format("Ya existe una manguera de %s en el lado %s de este surtidor. " +
                    "Por favor, elija otro lado o cambie el tipo de combustible.",
                    request.fuelType(), sideText)
            );
        }

        Nozzle nozzle = new Nozzle();
        nozzle.setPump(pump);
        nozzle.setSide(side);
        nozzle.setPosition(request.position());
        nozzle.setFuelType(fuelType);
        nozzle.setFuelName(request.fuelName()); // Nombre personalizado del combustible
        // No establecer precio aquí - se obtiene dinámicamente de FuelPriceHistory
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
        nozzle.setFuelName(request.fuelName());
        // No actualizar precio aquí - se maneja en FuelPriceHistory
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

    // ==================== DECIMAL CONFIGURATION ====================

    /**
     * Actualizar configuración de decimales de una manguera
     */
    @Transactional
    public NozzleDTO.DecimalsConfigResponse updateDecimalsConfig(Long id, NozzleDTO.UpdateDecimalsRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        // Validar valores
        if (request.solesDecimals() != null && (request.solesDecimals() < 0 || request.solesDecimals() > 5)) {
            throw new IllegalArgumentException("solesDecimals debe estar entre 0 y 5");
        }
        if (request.gallonsDecimals() != null && (request.gallonsDecimals() < 0 || request.gallonsDecimals() > 5)) {
            throw new IllegalArgumentException("gallonsDecimals debe estar entre 0 y 5");
        }
        if (request.clockDecimals() != null && (request.clockDecimals() < 0 || request.clockDecimals() > 5)) {
            throw new IllegalArgumentException("clockDecimals debe estar entre 0 y 5");
        }

        String oldConfig = String.format("Soles:%d, Gallons:%d, Clock:%d",
            nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2,
            nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3,
            nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0
        );

        // Actualizar valores
        if (request.solesDecimals() != null) {
            nozzle.setSolesDecimals(request.solesDecimals());
        }
        if (request.gallonsDecimals() != null) {
            nozzle.setGallonsDecimals(request.gallonsDecimals());
        }
        if (request.clockDecimals() != null) {
            nozzle.setClockDecimals(request.clockDecimals());
        }

        nozzle = nozzleRepository.save(nozzle);

        String newConfig = String.format("Soles:%d, Gallons:%d, Clock:%d",
            nozzle.getSolesDecimals(),
            nozzle.getGallonsDecimals(),
            nozzle.getClockDecimals()
        );

        // Registrar cambio en historial
        recordConfigurationChange(
            nozzle,
            PumpConfigurationHistory.ConfigChangeType.DECIMALS_CONFIG,
            oldConfig,
            newConfig,
            request.changedById(),
            null,
            request.reason(),
            "Decimals configuration updated"
        );

        return new NozzleDTO.DecimalsConfigResponse(
            nozzle.getId(),
            nozzle.getSolesDecimals(),
            nozzle.getGallonsDecimals(),
            nozzle.getClockDecimals(),
            nozzle.getUpdatedAt()
        );
    }

    /**
     * Obtener configuración de decimales de una manguera
     */
    @Transactional(readOnly = true)
    public NozzleDTO.DecimalsConfigResponse getDecimalsConfig(Long id) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        return new NozzleDTO.DecimalsConfigResponse(
            nozzle.getId(),
            nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2,
            nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3,
            nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0,
            nozzle.getUpdatedAt()
        );
    }

    // ==================== COUNTER CONFIGURATION ====================

    /**
     * Actualizar configuración completa de contadores de una manguera
     */
    @Transactional
    public NozzleDTO.CounterConfigResponse updateCounterConfig(Long id, NozzleDTO.UpdateCounterConfigRequest request) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        String oldConfig = getCurrentConfigString(nozzle);

        // Actualizar tipo de lectura principal
        if (request.readingType() != null) {
            nozzle.setReadingType(Nozzle.ReadingType.valueOf(request.readingType()));
        }

        // Actualizar configuración de SOLES
        if (request.soles() != null) {
            validateCounterConfig(request.soles(), "soles");
            nozzle.setHasSolesCounter(request.soles().enabled());
            nozzle.setSolesTotalDigits(request.soles().totalDigits());
            nozzle.setSolesDecimals(request.soles().decimalDigits());
        }

        // Actualizar configuración de GALONES
        if (request.gallons() != null) {
            validateCounterConfig(request.gallons(), "gallons");
            nozzle.setHasGallonsCounter(request.gallons().enabled());
            nozzle.setGallonsTotalDigits(request.gallons().totalDigits());
            nozzle.setGallonsDecimals(request.gallons().decimalDigits());
        }

        // Actualizar configuración de LITROS
        if (request.liters() != null) {
            validateCounterConfig(request.liters(), "liters");
            nozzle.setHasLitersCounter(request.liters().enabled());
            nozzle.setLitersTotalDigits(request.liters().totalDigits());
            nozzle.setLitersDecimals(request.liters().decimalDigits());
        }

        // Actualizar configuración de RELOJ
        if (request.clock() != null) {
            validateCounterConfig(request.clock(), "clock");
            nozzle.setHasClockCounter(request.clock().enabled());
            nozzle.setClockTotalDigits(request.clock().totalDigits());
            nozzle.setClockDecimals(request.clock().decimalDigits());
        }

        nozzle = nozzleRepository.save(nozzle);

        String newConfig = getCurrentConfigString(nozzle);

        // Registrar cambio en historial
        recordConfigurationChange(
            nozzle,
            PumpConfigurationHistory.ConfigChangeType.DECIMALS_CONFIG,
            oldConfig,
            newConfig,
            request.changedById(),
            null,
            request.reason(),
            "Counter configuration updated"
        );

        return toCounterConfigResponse(nozzle);
    }

    /**
     * Obtener configuración de contadores de una manguera
     */
    @Transactional(readOnly = true)
    public NozzleDTO.CounterConfigResponse getCounterConfig(Long id) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        return toCounterConfigResponse(nozzle);
    }

    /**
     * Obtener manguera con configuración completa
     */
    @Transactional(readOnly = true)
    public NozzleDTO.NozzleWithConfigResponse getNozzleWithConfig(Long id) {
        Nozzle nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nozzle not found with id: " + id));

        return toNozzleWithConfigResponse(nozzle);
    }

    /**
     * Obtener todas las mangueras de un surtidor con configuración
     */
    @Transactional(readOnly = true)
    public List<NozzleDTO.NozzleWithConfigResponse> getNozzlesWithConfigByPump(Long pumpId) {
        return nozzleRepository.findByPumpIdOrderByPosition(pumpId).stream()
            .map(this::toNozzleWithConfigResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener todas las mangueras de una isla con configuración
     */
    @Transactional(readOnly = true)
    public List<NozzleDTO.NozzleWithConfigResponse> getNozzlesWithConfigByIsland(Long islandId) {
        return nozzleRepository.findByPumpIslandIdOrderByPumpPositionAscPositionAsc(islandId).stream()
            .map(this::toNozzleWithConfigResponse)
            .collect(Collectors.toList());
    }

    private void validateCounterConfig(NozzleDTO.CounterConfig config, String counterName) {
        if (config.enabled() != null && config.enabled()) {
            if (config.totalDigits() == null || config.totalDigits() < 1 || config.totalDigits() > 12) {
                throw new IllegalArgumentException(
                    counterName + ": totalDigits debe estar entre 1 y 12 cuando el contador está habilitado"
                );
            }
            if (config.decimalDigits() == null || config.decimalDigits() < 0 || config.decimalDigits() > 6) {
                throw new IllegalArgumentException(
                    counterName + ": decimalDigits debe estar entre 0 y 6"
                );
            }
            if (config.decimalDigits() >= config.totalDigits()) {
                throw new IllegalArgumentException(
                    counterName + ": decimalDigits debe ser menor que totalDigits"
                );
            }
        }
    }

    private String getCurrentConfigString(Nozzle nozzle) {
        return String.format(
            "Type:%s, Soles:%s(%d.%d), Gallons:%s(%d.%d), Liters:%s(%d.%d), Clock:%s(%d.%d)",
            nozzle.getReadingType() != null ? nozzle.getReadingType().name() : "SOLES",
            nozzle.getHasSolesCounter() != null && nozzle.getHasSolesCounter() ? "ON" : "OFF",
            nozzle.getSolesTotalDigits() != null ? nozzle.getSolesTotalDigits() : 6,
            nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2,
            nozzle.getHasGallonsCounter() != null && nozzle.getHasGallonsCounter() ? "ON" : "OFF",
            nozzle.getGallonsTotalDigits() != null ? nozzle.getGallonsTotalDigits() : 6,
            nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3,
            nozzle.getHasLitersCounter() != null && nozzle.getHasLitersCounter() ? "ON" : "OFF",
            nozzle.getLitersTotalDigits() != null ? nozzle.getLitersTotalDigits() : 6,
            nozzle.getLitersDecimals() != null ? nozzle.getLitersDecimals() : 2,
            nozzle.getHasClockCounter() != null && nozzle.getHasClockCounter() ? "ON" : "OFF",
            nozzle.getClockTotalDigits() != null ? nozzle.getClockTotalDigits() : 8,
            nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0
        );
    }

    private NozzleDTO.CounterConfigResponse toCounterConfigResponse(Nozzle nozzle) {
        return new NozzleDTO.CounterConfigResponse(
            nozzle.getId(),
            nozzle.getReadingType() != null ? nozzle.getReadingType().name() : "SOLES",
            new NozzleDTO.CounterConfig(
                nozzle.getHasSolesCounter() != null ? nozzle.getHasSolesCounter() : true,
                nozzle.getSolesTotalDigits() != null ? nozzle.getSolesTotalDigits() : 6,
                nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasGallonsCounter() != null ? nozzle.getHasGallonsCounter() : true,
                nozzle.getGallonsTotalDigits() != null ? nozzle.getGallonsTotalDigits() : 6,
                nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasLitersCounter() != null ? nozzle.getHasLitersCounter() : false,
                nozzle.getLitersTotalDigits() != null ? nozzle.getLitersTotalDigits() : 6,
                nozzle.getLitersDecimals() != null ? nozzle.getLitersDecimals() : 2
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasClockCounter() != null ? nozzle.getHasClockCounter() : false,
                nozzle.getClockTotalDigits() != null ? nozzle.getClockTotalDigits() : 8,
                nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0
            ),
            nozzle.getUpdatedAt()
        );
    }

    private NozzleDTO.NozzleWithConfigResponse toNozzleWithConfigResponse(Nozzle nozzle) {
        return new NozzleDTO.NozzleWithConfigResponse(
            nozzle.getId(),
            nozzle.getPump().getId(),
            nozzle.getPump().getName(),
            nozzle.getPump().getIsland().getId(),
            nozzle.getPump().getIsland().getName(),
            nozzle.getSide().name(),
            nozzle.getPosition(),
            nozzle.getFuelType().name(),
            nozzle.getFuelName(),
            nozzle.getColor(),
            nozzle.getActive(),
            nozzle.getReadingType() != null ? nozzle.getReadingType().name() : "SOLES",
            new NozzleDTO.CounterConfig(
                nozzle.getHasSolesCounter() != null ? nozzle.getHasSolesCounter() : true,
                nozzle.getSolesTotalDigits() != null ? nozzle.getSolesTotalDigits() : 6,
                nozzle.getSolesDecimals() != null ? nozzle.getSolesDecimals() : 2
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasGallonsCounter() != null ? nozzle.getHasGallonsCounter() : true,
                nozzle.getGallonsTotalDigits() != null ? nozzle.getGallonsTotalDigits() : 6,
                nozzle.getGallonsDecimals() != null ? nozzle.getGallonsDecimals() : 3
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasLitersCounter() != null ? nozzle.getHasLitersCounter() : false,
                nozzle.getLitersTotalDigits() != null ? nozzle.getLitersTotalDigits() : 6,
                nozzle.getLitersDecimals() != null ? nozzle.getLitersDecimals() : 2
            ),
            new NozzleDTO.CounterConfig(
                nozzle.getHasClockCounter() != null ? nozzle.getHasClockCounter() : false,
                nozzle.getClockTotalDigits() != null ? nozzle.getClockTotalDigits() : 8,
                nozzle.getClockDecimals() != null ? nozzle.getClockDecimals() : 0
            ),
            nozzle.getCreatedAt(),
            nozzle.getUpdatedAt()
        );
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
            nozzle.getFuelName(),
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
            nozzle.getFuelName(),
            nozzle.getPricePerGallon() != null ? nozzle.getPricePerGallon().doubleValue() : null,
            nozzle.getColor(),
            nozzle.getActive(),
            nozzle.getCreatedAt(),
            nozzle.getUpdatedAt()
        );
    }
}
