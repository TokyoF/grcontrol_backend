package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.PumpConfigDTO;
import com.grcontrol.grcontrol_backend.entity.*;
import com.grcontrol.grcontrol_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gestión de configuración de surtidores y mangueras
 * Usado por ADMINISTRADOR para configurar precios y parámetros de contadores
 */
@Service
@RequiredArgsConstructor
public class PumpConfigService {

    private final PumpRepository pumpRepository;
    private final NozzleRepository nozzleRepository;
    private final IslandRepository islandRepository;
    private final StationRepository stationRepository;
    private final FuelPriceHistoryRepository fuelPriceHistoryRepository;
    private final UserRepository userRepository;

    // ==================== GESTIÓN DE ISLAS ====================

    /**
     * Obtener todas las islas de una estación
     */
    @Transactional(readOnly = true)
    public List<PumpConfigDTO.IslandResponse> getAllIslandsByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada"));
        
        return station.getIslands().stream()
            .map(this::mapToIslandResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener una isla por ID
     */
    @Transactional(readOnly = true)
    public PumpConfigDTO.IslandResponse getIslandById(Long islandId) {
        Island island = islandRepository.findById(islandId)
            .orElseThrow(() -> new IllegalArgumentException("Isla no encontrada"));
        return mapToIslandResponse(island);
    }

    /**
     * Crear una nueva isla
     */
    @Transactional
    public PumpConfigDTO.IslandResponse createIsland(Long stationId, PumpConfigDTO.IslandRequest request) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada"));

        Island island = new Island();
        island.setStation(station);
        island.setName(request.name());
        island.setDescription(request.description());
        island.setPosition(request.position());
        island.setStatus(request.status() != null ? 
            Island.IslandStatus.valueOf(request.status()) : Island.IslandStatus.ACTIVE);
        island.setActive(request.active() != null ? request.active() : true);

        Island saved = islandRepository.save(island);
        return mapToIslandResponse(saved);
    }

    /**
     * Actualizar una isla
     */
    @Transactional
    public PumpConfigDTO.IslandResponse updateIsland(Long islandId, PumpConfigDTO.IslandRequest request) {
        Island island = islandRepository.findById(islandId)
            .orElseThrow(() -> new IllegalArgumentException("Isla no encontrada"));

        if (request.name() != null) island.setName(request.name());
        if (request.description() != null) island.setDescription(request.description());
        if (request.position() != null) island.setPosition(request.position());
        if (request.status() != null) island.setStatus(Island.IslandStatus.valueOf(request.status()));
        if (request.active() != null) island.setActive(request.active());

        Island saved = islandRepository.save(island);
        return mapToIslandResponse(saved);
    }

    // ==================== GESTIÓN DE SURTIDORES ====================

    /**
     * Obtener todos los surtidores de una isla
     */
    @Transactional(readOnly = true)
    public List<PumpConfigDTO.PumpResponse> getAllPumpsByIsland(Long islandId) {
        return pumpRepository.findByIslandIdWithNozzles(islandId).stream()
            .map(this::mapToPumpResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener un surtidor por ID
     */
    @Transactional(readOnly = true)
    public PumpConfigDTO.PumpResponse getPumpById(Long pumpId) {
        Pump pump = pumpRepository.findByIdWithNozzles(pumpId)
            .orElseThrow(() -> new IllegalArgumentException("Surtidor no encontrado"));
        return mapToPumpResponse(pump);
    }

    /**
     * Crear un nuevo surtidor
     */
    @Transactional
    public PumpConfigDTO.PumpResponse createPump(PumpConfigDTO.PumpRequest request) {
        Island island = islandRepository.findById(request.islandId())
            .orElseThrow(() -> new IllegalArgumentException("Isla no encontrada"));

        Pump pump = new Pump();
        pump.setIsland(island);
        pump.setName(request.name());
        pump.setPosition(request.position());
        pump.setBrand(request.brand());
        pump.setModel(request.model());
        pump.setSerialNumber(request.serialNumber());
        if (request.installationDate() != null) {
            pump.setInstallationDate(LocalDate.parse(request.installationDate()));
        }
        pump.setNotes(request.notes());
        pump.setStatus(request.status() != null ? 
            Pump.PumpStatus.valueOf(request.status()) : Pump.PumpStatus.ACTIVE);
        pump.setActive(request.active() != null ? request.active() : true);

        Pump saved = pumpRepository.save(pump);
        return mapToPumpResponse(saved);
    }

    /**
     * Actualizar un surtidor
     */
    @Transactional
    public PumpConfigDTO.PumpResponse updatePump(Long pumpId, PumpConfigDTO.PumpRequest request) {
        Pump pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new IllegalArgumentException("Surtidor no encontrado"));

        if (request.name() != null) pump.setName(request.name());
        if (request.position() != null) pump.setPosition(request.position());
        if (request.brand() != null) pump.setBrand(request.brand());
        if (request.model() != null) pump.setModel(request.model());
        if (request.serialNumber() != null) pump.setSerialNumber(request.serialNumber());
        if (request.installationDate() != null) {
            pump.setInstallationDate(LocalDate.parse(request.installationDate()));
        }
        if (request.notes() != null) pump.setNotes(request.notes());
        if (request.status() != null) pump.setStatus(Pump.PumpStatus.valueOf(request.status()));
        if (request.active() != null) pump.setActive(request.active());

        Pump saved = pumpRepository.save(pump);
        return mapToPumpResponse(saved);
    }

    /**
     * Eliminar un surtidor
     */
    @Transactional
    public void deletePump(Long pumpId) {
        Pump pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new IllegalArgumentException("Surtidor no encontrado"));
        pumpRepository.delete(pump);
    }

    // ==================== GESTIÓN DE MANGUERAS ====================

    /**
     * Obtener todas las mangueras de un surtidor
     */
    @Transactional(readOnly = true)
    public List<PumpConfigDTO.NozzleConfigResponse> getAllNozzlesByPump(Long pumpId) {
        Pump pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new IllegalArgumentException("Surtidor no encontrado"));
        
        return nozzleRepository.findByPumpIdOrderByPosition(pumpId).stream()
            .map(nozzle -> mapToNozzleConfigResponse(nozzle, pump.getIsland().getStation().getId()))
            .collect(Collectors.toList());
    }

    /**
     * Obtener una manguera por ID
     */
    @Transactional(readOnly = true)
    public PumpConfigDTO.NozzleConfigResponse getNozzleById(Long nozzleId) {
        Nozzle nozzle = nozzleRepository.findById(nozzleId)
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada"));
        return mapToNozzleConfigResponse(nozzle, nozzle.getPump().getIsland().getStation().getId());
    }

    /**
     * Crear una nueva manguera
     */
    @Transactional
    public PumpConfigDTO.NozzleConfigResponse createNozzle(Long pumpId, PumpConfigDTO.NozzleConfigRequest request) {
        Pump pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new IllegalArgumentException("Surtidor no encontrado"));

        Nozzle nozzle = new Nozzle();
        nozzle.setPump(pump);
        updateNozzleFromRequest(nozzle, request);

        Nozzle saved = nozzleRepository.save(nozzle);
        return mapToNozzleConfigResponse(saved, pump.getIsland().getStation().getId());
    }

    /**
     * Actualizar configuración de una manguera
     */
    @Transactional
    public PumpConfigDTO.NozzleConfigResponse updateNozzle(Long nozzleId, PumpConfigDTO.NozzleConfigRequest request) {
        Nozzle nozzle = nozzleRepository.findById(nozzleId)
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada"));

        updateNozzleFromRequest(nozzle, request);

        Nozzle saved = nozzleRepository.save(nozzle);
        return mapToNozzleConfigResponse(saved, nozzle.getPump().getIsland().getStation().getId());
    }

    /**
     * Eliminar una manguera
     */
    @Transactional
    public void deleteNozzle(Long nozzleId) {
        Nozzle nozzle = nozzleRepository.findById(nozzleId)
            .orElseThrow(() -> new IllegalArgumentException("Manguera no encontrada"));
        nozzleRepository.delete(nozzle);
    }

    // ==================== GESTIÓN DE PRECIOS ====================

    /**
     * Obtener precios actuales de todos los combustibles de una estación
     */
    @Transactional(readOnly = true)
    public List<PumpConfigDTO.FuelPriceResponse> getCurrentPrices(Long stationId) {
        return fuelPriceHistoryRepository.findAllCurrentPrices(stationId).stream()
            .map(this::mapToFuelPriceResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener historial de precios de un combustible
     */
    @Transactional(readOnly = true)
    public List<PumpConfigDTO.FuelPriceResponse> getPriceHistory(Long stationId, String fuelTypeStr) {
        FuelPriceHistory.FuelType fuelType = FuelPriceHistory.FuelType.valueOf(fuelTypeStr);
        return fuelPriceHistoryRepository.findHistoryByStationAndFuelType(stationId, fuelType).stream()
            .map(this::mapToFuelPriceResponse)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar precio de un combustible
     * Marca el precio anterior como inactivo y crea uno nuevo
     */
    @Transactional
    public PumpConfigDTO.FuelPriceResponse updateFuelPrice(Long stationId, 
                                                           PumpConfigDTO.FuelPriceUpdateRequest request,
                                                           String username) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada"));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        FuelPriceHistory.FuelType fuelType = FuelPriceHistory.FuelType.valueOf(request.fuelType());

        // Marcar el precio actual como inactivo
        fuelPriceHistoryRepository.findCurrentPrice(stationId, fuelType).ifPresent(currentPrice -> {
            currentPrice.setEffectiveUntil(LocalDateTime.now());
            fuelPriceHistoryRepository.save(currentPrice);
        });

        // Crear nuevo precio
        FuelPriceHistory newPrice = new FuelPriceHistory();
        newPrice.setStation(station);
        newPrice.setFuelType(fuelType);
        newPrice.setPricePerGallon(request.pricePerGallon());
        newPrice.setEffectiveFrom(LocalDateTime.now());
        newPrice.setChangedBy(user);
        newPrice.setNotes(request.notes());

        FuelPriceHistory saved = fuelPriceHistoryRepository.save(newPrice);
        return mapToFuelPriceResponse(saved);
    }

    // ==================== MÉTODOS DE MAPEO ====================

    private PumpConfigDTO.IslandResponse mapToIslandResponse(Island island) {
        List<PumpConfigDTO.PumpResponse> pumps = island.getPumps() != null ? 
            island.getPumps().stream()
                .map(this::mapToPumpResponse)
                .collect(Collectors.toList()) : 
            List.of();

        return new PumpConfigDTO.IslandResponse(
            island.getId(),
            island.getStation().getName(),
            island.getName(),
            island.getDescription(),
            island.getPosition(),
            island.getStatus().name(),
            island.getActive(),
            pumps,
            island.getCreatedAt(),
            island.getUpdatedAt()
        );
    }

    private PumpConfigDTO.PumpResponse mapToPumpResponse(Pump pump) {
        List<PumpConfigDTO.NozzleConfigResponse> nozzles = pump.getNozzles() != null ?
            pump.getNozzles().stream()
                .map(nozzle -> mapToNozzleConfigResponse(nozzle, pump.getIsland().getStation().getId()))
                .collect(Collectors.toList()) :
            List.of();

        return new PumpConfigDTO.PumpResponse(
            pump.getId(),
            pump.getIsland().getId(),
            pump.getIsland().getName(),
            pump.getName(),
            pump.getPosition(),
            pump.getBrand(),
            pump.getModel(),
            pump.getSerialNumber(),
            pump.getInstallationDate() != null ? pump.getInstallationDate().toString() : null,
            pump.getNotes(),
            pump.getStatus().name(),
            pump.getActive(),
            nozzles,
            pump.getCreatedAt(),
            pump.getUpdatedAt()
        );
    }

    private PumpConfigDTO.NozzleConfigResponse mapToNozzleConfigResponse(Nozzle nozzle, Long stationId) {
        // Obtener precio actual del combustible
        BigDecimal currentPrice = fuelPriceHistoryRepository
            .findCurrentPrice(stationId, FuelPriceHistory.FuelType.valueOf(nozzle.getFuelType().name()))
            .map(FuelPriceHistory::getPricePerGallon)
            .orElse(nozzle.getPricePerGallon()); // Fallback al precio legacy

        return new PumpConfigDTO.NozzleConfigResponse(
            nozzle.getId(),
            nozzle.getPump().getId(),
            nozzle.getFuelType().name(),
            nozzle.getFuelName(),
            nozzle.getColor(),
            nozzle.getSide().name(),
            nozzle.getPosition(),
            currentPrice,
            nozzle.getReadingType() != null ? nozzle.getReadingType().name() : null,
            nozzle.getSolesTotalDigits(),
            nozzle.getSolesDecimals(),
            nozzle.getGallonsTotalDigits(),
            nozzle.getGallonsDecimals(),
            nozzle.getLitersTotalDigits(),
            nozzle.getLitersDecimals(),
            nozzle.getClockTotalDigits(),
            nozzle.getClockDecimals(),
            nozzle.getHasSolesCounter(),
            nozzle.getHasGallonsCounter(),
            nozzle.getHasLitersCounter(),
            nozzle.getHasClockCounter(),
            nozzle.getStatus().name(),
            nozzle.getActive(),
            nozzle.getCreatedAt(),
            nozzle.getUpdatedAt()
        );
    }

    private void updateNozzleFromRequest(Nozzle nozzle, PumpConfigDTO.NozzleConfigRequest request) {
        if (request.fuelType() != null) 
            nozzle.setFuelType(Nozzle.FuelType.valueOf(request.fuelType()));
        if (request.fuelName() != null) 
            nozzle.setFuelName(request.fuelName());
        if (request.color() != null) 
            nozzle.setColor(request.color());
        if (request.side() != null) 
            nozzle.setSide(Nozzle.PumpSide.valueOf(request.side()));
        if (request.position() != null) 
            nozzle.setPosition(request.position());

        // Configuración de contadores
        if (request.readingType() != null) 
            nozzle.setReadingType(Nozzle.ReadingType.valueOf(request.readingType()));
        
        if (request.solesTotalDigits() != null) 
            nozzle.setSolesTotalDigits(request.solesTotalDigits());
        if (request.solesDecimals() != null) 
            nozzle.setSolesDecimals(request.solesDecimals());
        
        if (request.gallonsTotalDigits() != null) 
            nozzle.setGallonsTotalDigits(request.gallonsTotalDigits());
        if (request.gallonsDecimals() != null) 
            nozzle.setGallonsDecimals(request.gallonsDecimals());
        
        if (request.litersTotalDigits() != null) 
            nozzle.setLitersTotalDigits(request.litersTotalDigits());
        if (request.litersDecimals() != null) 
            nozzle.setLitersDecimals(request.litersDecimals());
        
        if (request.clockTotalDigits() != null) 
            nozzle.setClockTotalDigits(request.clockTotalDigits());
        if (request.clockDecimals() != null) 
            nozzle.setClockDecimals(request.clockDecimals());

        // Flags de contadores habilitados
        if (request.hasSolesCounter() != null) 
            nozzle.setHasSolesCounter(request.hasSolesCounter());
        if (request.hasGallonsCounter() != null) 
            nozzle.setHasGallonsCounter(request.hasGallonsCounter());
        if (request.hasLitersCounter() != null) 
            nozzle.setHasLitersCounter(request.hasLitersCounter());
        if (request.hasClockCounter() != null) 
            nozzle.setHasClockCounter(request.hasClockCounter());

        // Estado
        if (request.status() != null) 
            nozzle.setStatus(Nozzle.NozzleStatus.valueOf(request.status()));
        if (request.active() != null) 
            nozzle.setActive(request.active());
    }

    private PumpConfigDTO.FuelPriceResponse mapToFuelPriceResponse(FuelPriceHistory price) {
        return new PumpConfigDTO.FuelPriceResponse(
            price.getId(),
            price.getFuelType().name(),
            price.getPricePerGallon(),
            price.getEffectiveFrom(),
            price.getEffectiveUntil(),
            price.getChangedBy() != null ? price.getChangedBy().getUsername() : null,
            price.getNotes(),
            price.isCurrent()
        );
    }
}
