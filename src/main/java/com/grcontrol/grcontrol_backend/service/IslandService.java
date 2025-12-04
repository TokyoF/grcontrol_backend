package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.IslandDTO;
import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.FuelPriceHistory;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.repository.IslandRepository;
import com.grcontrol.grcontrol_backend.repository.StationRepository;
import com.grcontrol.grcontrol_backend.repository.FuelPriceHistoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de islas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IslandService {

    private final IslandRepository islandRepository;
    private final StationRepository stationRepository;
    private final FuelPriceHistoryRepository fuelPriceHistoryRepository;
    private final EntityManager entityManager;

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public IslandDTO.IslandResponse createIsland(IslandDTO.IslandRequest request) {
        Station station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + request.stationId()));

        // Validar nombre único en la estación
        if (islandRepository.existsByStationIdAndName(request.stationId(), request.name())) {
            throw new IllegalArgumentException("Island with name '" + request.name() +
                "' already exists in this station");
        }

        Island island = new Island();
        island.setStation(station);
        island.setName(request.name());
        island.setDescription(request.description());
        island.setStatus(request.status() != null
            ? Island.IslandStatus.valueOf(request.status())
            : Island.IslandStatus.ACTIVE);
        island.setPosition(request.position());

        island = islandRepository.save(island);

        return toIslandResponse(island);
    }

    @Transactional(readOnly = true)
    public IslandDTO.IslandResponse getIsland(Long id) {
        Island island = islandRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + id));

        return toIslandResponse(island);
    }

    @Transactional(readOnly = true)
    public IslandDTO.IslandDetailResponse getIslandDetail(Long id) {
        // Limpiar el caché de EntityManager para obtener datos frescos
        entityManager.clear();
        
        Island island = islandRepository.findByIdWithPumpsAndNozzles(id)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + id));

        return toIslandDetailResponse(island);
    }

    @Transactional(readOnly = true)
    public List<IslandDTO.IslandResponse> getIslandsByStation(Long stationId) {
        log.info("Buscando islas para estación ID: {}", stationId);
        
        // Verificar que la estación existe
        if (!stationRepository.existsById(stationId)) {
            log.warn("Estación no encontrada con ID: {}", stationId);
            return List.of();
        }
        
        List<Island> islands = islandRepository.findAllByStationIdWithPumps(stationId);
        log.info("Encontradas {} islas", islands.size());
        
        // Mapear a DTO dentro de la transacción para evitar LazyInitializationException
        List<IslandDTO.IslandResponse> responses = islands.stream()
            .map(island -> {
                try {
                    // Forzar inicialización de la relación station
                    Station station = island.getStation();
                    if (station != null) {
                        station.getName(); // Forzar carga del proxy
                    }
                    return toIslandResponse(island);
                } catch (Exception e) {
                    log.error("Error mapeando isla ID {}: {}", island.getId(), e.getMessage(), e);
                    throw new RuntimeException("Error mapping island " + island.getId(), e);
                }
            })
            .collect(Collectors.toList());
        
        log.info("Mapeadas {} islas a DTOs exitosamente", responses.size());
        return responses;
    }

    @Transactional(readOnly = true)
    public List<IslandDTO.IslandResponse> getActiveIslandsByStation(Long stationId) {
        return islandRepository.findByStationIdWithPumps(stationId).stream()
            .map(this::toIslandResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public IslandDTO.IslandResponse updateIsland(Long id, IslandDTO.IslandRequest request) {
        Island island = islandRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + id));

        // Validar nombre único si cambió
        if (!island.getName().equals(request.name()) &&
            islandRepository.existsByStationIdAndName(request.stationId(), request.name())) {
            throw new IllegalArgumentException("Island with name '" + request.name() +
                "' already exists in this station");
        }

        // Actualizar estación si cambió
        if (!island.getStation().getId().equals(request.stationId())) {
            Station newStation = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + request.stationId()));
            island.setStation(newStation);
        }

        island.setName(request.name());
        island.setDescription(request.description());
        if (request.status() != null) {
            island.setStatus(Island.IslandStatus.valueOf(request.status()));
        }
        island.setPosition(request.position());

        island = islandRepository.save(island);

        return toIslandResponse(island);
    }

    @Transactional
    public IslandDTO.OperationResponse changeStatus(Long id, IslandDTO.ChangeStatusRequest request) {
        Island island = islandRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + id));

        Island.IslandStatus newStatus = Island.IslandStatus.valueOf(request.status());
        island.setStatus(newStatus);

        // Agregar razón a la descripción si existe
        if (request.reason() != null && !request.reason().isEmpty()) {
            String currentDesc = island.getDescription() != null ? island.getDescription() : "";
            island.setDescription(currentDesc + "\nStatus change: " + request.reason());
        }

        islandRepository.save(island);

        return new IslandDTO.OperationResponse(
            true,
            "Island status changed to " + newStatus,
            id
        );
    }

    @Transactional
    public void deleteIsland(Long id) {
        if (!islandRepository.existsById(id)) {
            throw new IllegalArgumentException("Island not found with id: " + id);
        }
        islandRepository.deleteById(id);
    }

    // ==================== HELPERS ====================

    private IslandDTO.IslandResponse toIslandResponse(Island island) {
        try {
            log.debug("Convirtiendo isla a DTO: ID={}", island.getId());
            
            Station station = island.getStation();
            if (station == null) {
                log.error("Station is NULL for Island ID: {}", island.getId());
                throw new IllegalStateException("Island " + island.getId() + " has no associated station");
            }
            
            return new IslandDTO.IslandResponse(
                island.getId(),
                station.getId(),
                station.getName(),
                island.getName(),
                island.getDescription(),
                island.getStatus().name(),
                island.getPosition(),
                island.getPumps() != null ? island.getPumps().size() : 0,
                island.getCreatedAt(),
                island.getUpdatedAt()
            );
        } catch (Exception e) {
            log.error("Error convirtiendo isla ID {} a DTO: {}", island.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private IslandDTO.IslandDetailResponse toIslandDetailResponse(Island island) {
        Long stationId = island.getStation().getId();
        
        List<IslandDTO.PumpWithNozzles> pumps = island.getPumps() != null
            ? island.getPumps().stream()
                .map(pump -> new IslandDTO.PumpWithNozzles(
                    pump.getId(),
                    pump.getName(),
                    pump.getPosition(),
                    pump.getBrand(),
                    pump.getModel(),
                    pump.getActive(),
                    pump.getNozzles() != null
                        ? pump.getNozzles().stream()
                            .map(nozzle -> {
                                // Obtener precio dinámicamente desde FuelPriceHistory
                                Double price = null;
                                try {
                                    FuelPriceHistory.FuelType fuelType = FuelPriceHistory.FuelType.valueOf(
                                        nozzle.getFuelType().name()
                                    );
                                    BigDecimal pricePerGallon = fuelPriceHistoryRepository
                                        .findCurrentPrice(stationId, fuelType)
                                        .map(FuelPriceHistory::getPricePerGallon)
                                        .orElse(null);
                                    price = pricePerGallon != null ? pricePerGallon.doubleValue() : null;
                                } catch (Exception e) {
                                    log.warn("Error getting price for fuel type {}: {}", 
                                        nozzle.getFuelType(), e.getMessage());
                                }
                                
                                return new IslandDTO.NozzleSummary(
                                    nozzle.getId(),
                                    nozzle.getSide().name(),
                                    nozzle.getPosition(),
                                    nozzle.getFuelType().name(),
                                    nozzle.getFuelName(),
                                    price, // Precio dinámico desde FuelPriceHistory
                                    nozzle.getColor(),
                                    nozzle.getActive()
                                );
                            })
                            .collect(Collectors.toList())
                        : List.of()
                ))
                .collect(Collectors.toList())
            : List.of();

        return new IslandDTO.IslandDetailResponse(
            island.getId(),
            island.getStation().getId(),
            island.getStation().getName(),
            island.getName(),
            island.getDescription(),
            island.getStatus().name(),
            island.getPosition(),
            pumps,
            island.getCreatedAt(),
            island.getUpdatedAt()
        );
    }
}
