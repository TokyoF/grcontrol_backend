package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.IslandDTO;
import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.repository.IslandRepository;
import com.grcontrol.grcontrol_backend.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de islas
 */
@Service
@RequiredArgsConstructor
public class IslandService {

    private final IslandRepository islandRepository;
    private final StationRepository stationRepository;

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
        Island island = islandRepository.findByIdWithPumpsAndNozzles(id)
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + id));

        return toIslandDetailResponse(island);
    }

    @Transactional(readOnly = true)
    public List<IslandDTO.IslandResponse> getIslandsByStation(Long stationId) {
        return islandRepository.findAllByStationIdWithPumps(stationId).stream()
            .map(this::toIslandResponse)
            .collect(Collectors.toList());
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
        return new IslandDTO.IslandResponse(
            island.getId(),
            island.getStation().getId(),
            island.getStation().getName(),
            island.getName(),
            island.getDescription(),
            island.getStatus().name(),
            island.getPosition(),
            island.getPumps() != null ? island.getPumps().size() : 0,
            island.getCreatedAt(),
            island.getUpdatedAt()
        );
    }

    private IslandDTO.IslandDetailResponse toIslandDetailResponse(Island island) {
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
                            .map(nozzle -> new IslandDTO.NozzleSummary(
                                nozzle.getId(),
                                nozzle.getSide().name(),
                                nozzle.getPosition(),
                                nozzle.getFuelType().name(),
                                nozzle.getPricePerGallon() != null
                                    ? nozzle.getPricePerGallon().doubleValue()
                                    : null,
                                nozzle.getColor(),
                                nozzle.getActive()
                            ))
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
