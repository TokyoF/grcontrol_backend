package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.PumpDTO;
import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.Pump;
import com.grcontrol.grcontrol_backend.repository.IslandRepository;
import com.grcontrol.grcontrol_backend.repository.PumpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de surtidores
 */
@Service
@RequiredArgsConstructor
public class PumpService {

    private final PumpRepository pumpRepository;
    private final IslandRepository islandRepository;

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public PumpDTO.PumpResponse createPump(PumpDTO.PumpRequest request) {
        Island island = islandRepository.findById(request.islandId())
            .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + request.islandId()));

        Pump pump = new Pump();
        pump.setIsland(island);
        pump.setName(request.name());
        pump.setPosition(request.position());
        pump.setBrand(request.brand());
        pump.setModel(request.model());
        pump.setSerialNumber(request.serialNumber());
        pump.setInstallationDate(request.installationDate());
        pump.setActive(request.isActive() != null ? request.isActive() : true);
        pump.setNotes(request.notes());

        pump = pumpRepository.save(pump);

        return toPumpResponse(pump);
    }

    @Transactional(readOnly = true)
    public PumpDTO.PumpResponse getPump(Long id) {
        Pump pump = pumpRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + id));

        return toPumpResponse(pump);
    }

    @Transactional(readOnly = true)
    public PumpDTO.PumpDetailResponse getPumpDetail(Long id) {
        Pump pump = pumpRepository.findByIdWithNozzles(id)
            .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + id));

        return toPumpDetailResponse(pump);
    }

    @Transactional(readOnly = true)
    public List<PumpDTO.PumpResponse> getPumpsByIsland(Long islandId) {
        return pumpRepository.findByIslandId(islandId).stream()
            .map(this::toPumpResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PumpDTO.PumpResponse> getActivePumpsByIsland(Long islandId) {
        return pumpRepository.findByIslandIdAndActiveTrue(islandId).stream()
            .map(this::toPumpResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PumpDTO.PumpResponse> getActivePumpsByStation(Long stationId) {
        return pumpRepository.findActiveByStationId(stationId).stream()
            .map(this::toPumpResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PumpDTO.PumpResponse updatePump(Long id, PumpDTO.PumpRequest request) {
        Pump pump = pumpRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + id));

        // Actualizar isla si cambió
        if (!pump.getIsland().getId().equals(request.islandId())) {
            Island newIsland = islandRepository.findById(request.islandId())
                .orElseThrow(() -> new IllegalArgumentException("Island not found with id: " + request.islandId()));
            pump.setIsland(newIsland);
        }

        pump.setName(request.name());
        pump.setPosition(request.position());
        pump.setBrand(request.brand());
        pump.setModel(request.model());
        pump.setSerialNumber(request.serialNumber());
        pump.setInstallationDate(request.installationDate());
        if (request.isActive() != null) {
            pump.setActive(request.isActive());
        }
        pump.setNotes(request.notes());

        pump = pumpRepository.save(pump);

        return toPumpResponse(pump);
    }

    @Transactional
    public PumpDTO.OperationResponse toggleActive(Long id, PumpDTO.ToggleActiveRequest request) {
        Pump pump = pumpRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pump not found with id: " + id));

        pump.setActive(request.isActive());

        // Agregar razón a las notas si existe
        if (request.reason() != null && !request.reason().isEmpty()) {
            String currentNotes = pump.getNotes() != null ? pump.getNotes() : "";
            pump.setNotes(currentNotes + "\nStatus change: " + request.reason());
        }

        pumpRepository.save(pump);

        return new PumpDTO.OperationResponse(
            true,
            "Pump " + (request.isActive() ? "activated" : "deactivated") + " successfully",
            id
        );
    }

    @Transactional
    public void deletePump(Long id) {
        if (!pumpRepository.existsById(id)) {
            throw new IllegalArgumentException("Pump not found with id: " + id);
        }
        pumpRepository.deleteById(id);
    }

    // ==================== HELPERS ====================

    private PumpDTO.PumpResponse toPumpResponse(Pump pump) {
        return new PumpDTO.PumpResponse(
            pump.getId(),
            pump.getIsland().getId(),
            pump.getIsland().getName(),
            pump.getName(),
            pump.getPosition(),
            pump.getBrand(),
            pump.getModel(),
            pump.getSerialNumber(),
            pump.getInstallationDate(),
            pump.getActive(),
            pump.getNozzles() != null ? pump.getNozzles().size() : 0,
            pump.getNotes(),
            pump.getCreatedAt(),
            pump.getUpdatedAt()
        );
    }

    private PumpDTO.PumpDetailResponse toPumpDetailResponse(Pump pump) {
        List<PumpDTO.NozzleResponse> nozzles = pump.getNozzles() != null
            ? pump.getNozzles().stream()
                .map(nozzle -> new PumpDTO.NozzleResponse(
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
            : List.of();

        return new PumpDTO.PumpDetailResponse(
            pump.getId(),
            pump.getIsland().getId(),
            pump.getIsland().getName(),
            pump.getName(),
            pump.getPosition(),
            pump.getBrand(),
            pump.getModel(),
            pump.getSerialNumber(),
            pump.getInstallationDate(),
            pump.getActive(),
            pump.getNotes(),
            nozzles,
            pump.getCreatedAt(),
            pump.getUpdatedAt()
        );
    }
}
