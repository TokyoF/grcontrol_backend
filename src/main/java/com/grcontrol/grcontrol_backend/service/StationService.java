package com.grcontrol.grcontrol_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grcontrol.grcontrol_backend.dto.StationDTO;
import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.Pump;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.repository.IslandRepository;
import com.grcontrol.grcontrol_backend.repository.NozzleRepository;
import com.grcontrol.grcontrol_backend.repository.PumpRepository;
import com.grcontrol.grcontrol_backend.repository.ShiftScheduleRepository;
import com.grcontrol.grcontrol_backend.repository.StationRepository;
import com.grcontrol.grcontrol_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestión de estaciones de servicio
 */
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final ShiftScheduleRepository shiftScheduleRepository;
    private final IslandRepository islandRepository;
    private final PumpRepository pumpRepository;
    private final NozzleRepository nozzleRepository;

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public StationDTO.StationResponse createStation(StationDTO.StationRequest request) {
        // Validar nombre único
        if (stationRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Station with name '" + request.name() + "' already exists");
        }

        Station station = new Station();
        station.setName(request.name());
        station.setAddress(request.address());
        station.setPhone(request.phone());
        station.setActive(request.isActive() != null ? request.isActive() : true);

        station = stationRepository.save(station);

        return toStationResponse(station);
    }

    @Transactional(readOnly = true)
    public StationDTO.StationResponse getStation(Long id) {
        Station station = stationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + id));

        return toStationResponse(station);
    }

    @Transactional(readOnly = true)
    public StationDTO.StationDetailResponse getStationDetail(Long id) {
        Station station = stationRepository.findByIdWithIslands(id)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + id));

        return toStationDetailResponse(station);
    }

    @Transactional(readOnly = true)
    public List<StationDTO.StationResponse> getAllStations() {
        return stationRepository.findAll().stream()
            .map(this::toStationResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StationDTO.StationResponse> getActiveStations() {
        return stationRepository.findByActiveTrue().stream()
            .map(this::toStationResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public StationDTO.StationResponse updateStation(Long id, StationDTO.StationRequest request) {
        Station station = stationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + id));

        // Validar nombre único si cambió
        if (!station.getName().equals(request.name()) &&
            stationRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Station with name '" + request.name() + "' already exists");
        }

        station.setName(request.name());
        station.setAddress(request.address());
        station.setPhone(request.phone());
        if (request.isActive() != null) {
            station.setActive(request.isActive());
        }

        station = stationRepository.save(station);

        return toStationResponse(station);
    }

    @Transactional
    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new IllegalArgumentException("Station not found with id: " + id);
        }
        stationRepository.deleteById(id);
    }

    // ==================== ADMINISTRATOR MANAGEMENT ====================

    @Transactional
    public StationDTO.OperationResponse assignAdministrators(Long stationId,StationDTO.AssignAdministratorsRequest request) {
        Station station = stationRepository.findByIdWithAdministrators(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + stationId));

        // Limpiar administradores actuales
        station.getAdministrators().clear();

        // Asignar nuevos administradores
        for (Long adminId : request.administratorIds()) {
            User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + adminId));

            // Verificar que el usuario sea administrador
            boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role.getName()));

            if (!isAdmin) {
                throw new IllegalArgumentException("User " + admin.getUsername() + " is not an administrator");
            }

            station.getAdministrators().add(admin);
        }

        stationRepository.save(station);

        return new StationDTO.OperationResponse(
            true,
            "Administrators assigned successfully",
            stationId
        );
    }

    @Transactional
    public StationDTO.OperationResponse addAdministrator(Long stationId, Long userId) {
        Station station = stationRepository.findByIdWithAdministrators(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + stationId));

        User admin = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Verificar que el usuario sea administrador
        boolean isAdmin = admin.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role.getName()));

        if (!isAdmin) {
            throw new IllegalArgumentException("User " + admin.getUsername() + " is not an administrator");
        }

        // Verificar si ya está asignado
        if (station.getAdministrators().contains(admin)) {
            throw new IllegalArgumentException("Administrator already assigned to this station");
        }

        station.getAdministrators().add(admin);
        stationRepository.save(station);

        return new StationDTO.OperationResponse(
            true,
            "Administrator added successfully",
            stationId
        );
    }

    @Transactional
    public StationDTO.OperationResponse removeAdministrator(Long stationId, Long userId) {
        Station station = stationRepository.findByIdWithAdministrators(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + stationId));

        User admin = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!station.getAdministrators().remove(admin)) {
            throw new IllegalArgumentException("Administrator not assigned to this station");
        }

        stationRepository.save(station);

        return new StationDTO.OperationResponse(
            true,
            "Administrator removed successfully",
            stationId
        );
    }

    @Transactional(readOnly = true)
    public List<StationDTO.StationResponse> getStationsByAdministrator(Long userId) {
        return stationRepository.findByAdministratorId(userId).stream()
            .map(this::toStationResponse)
            .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================

    private StationDTO.StationResponse toStationResponse(Station station) {
        return new StationDTO.StationResponse(
            station.getId(),
            station.getName(),
            station.getAddress(),
            station.getPhone(),
            station.getActive(),
            station.getIslands() != null ? station.getIslands().size() : 0,
            station.getAdministrators() != null ? station.getAdministrators().size() : 0,
            station.getCreatedAt(),
            station.getUpdatedAt()
        );
    }

    private StationDTO.StationDetailResponse toStationDetailResponse(Station station) {
        List<StationDTO.IslandSummary> islands = station.getIslands() != null
            ? station.getIslands().stream()
                .map(island -> new StationDTO.IslandSummary(
                    island.getId(),
                    island.getName(),
                    island.getStatus().name(),
                    island.getPumps() != null ? island.getPumps().size() : 0,
                    island.getPosition()
                ))
                .collect(Collectors.toList())
            : List.of();

        List<StationDTO.AdministratorSummary> administrators = station.getAdministrators() != null
            ? station.getAdministrators().stream()
                .map(admin -> new StationDTO.AdministratorSummary(
                    admin.getId(),
                    admin.getUsername(),
                    admin.getFirstName(),
                    admin.getLastName(),
                    admin.getEmail()
                ))
                .collect(Collectors.toList())
            : List.of();

        return new StationDTO.StationDetailResponse(
            station.getId(),
            station.getName(),
            station.getAddress(),
            station.getPhone(),
            station.getActive(),
            islands,
            administrators,
            station.getCreatedAt(),
            station.getUpdatedAt()
        );
    }

    // ==================== SHIFT SCHEDULES ====================

    /**
     * Obtener horarios de una estación
     */
    @Transactional(readOnly = true)
    public List<StationDTO.ShiftScheduleResponse> getStationSchedules(Long stationId) {
        // Verificar que la estación existe
        if (!stationRepository.existsById(stationId)) {
            throw new IllegalArgumentException("Station not found with id: " + stationId);
        }

        return shiftScheduleRepository.findByStationId(stationId).stream()
            .map(schedule -> new StationDTO.ShiftScheduleResponse(
                schedule.getId(),
                schedule.getDisplayLabel(),
                schedule.getName() != null ? schedule.getName() : schedule.getDisplayLabel(),
                schedule.getStartTime() != null ? schedule.getStartTime().toString() : "",
                schedule.getEndTime() != null ? schedule.getEndTime().toString() : "",
                schedule.getActive()
            ))
            .collect(Collectors.toList());
    }

    // ==================== FULL CONFIGURATION ====================

    /**
     * Obtener configuración completa de una estación (para móvil)
     */
    @Transactional(readOnly = true)
    public StationDTO.StationFullConfigResponse getStationFullConfiguration(Long stationId) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found with id: " + stationId));

        // Cargar islas con surtidores
        List<Island> islands = islandRepository.findByStationId(stationId);

        List<StationDTO.IslandFullConfig> islandConfigs = islands.stream()
            .map(island -> {
                // Cargar surtidores de la isla
                List<Pump> pumps = pumpRepository.findByIslandId(island.getId());

                List<StationDTO.PumpFullConfig> pumpConfigs = pumps.stream()
                    .map(pump -> {
                        // Cargar mangueras del surtidor
                        List<Nozzle> nozzles = nozzleRepository.findByPumpIdOrderByPosition(pump.getId());

                        List<StationDTO.NozzleFullConfig> nozzleConfigs = nozzles.stream()
                            .map(nozzle -> new StationDTO.NozzleFullConfig(
                                nozzle.getId(),
                                nozzle.getFuelName() != null ? nozzle.getFuelName() : nozzle.getFuelType().name(),
                                nozzle.getFuelType().name(),
                                nozzle.getSide().name(),
                                nozzle.getPosition(),
                                nozzle.getPricePerGallon(),
                                nozzle.getActive()
                            ))
                            .collect(Collectors.toList());

                        return new StationDTO.PumpFullConfig(
                            pump.getId(),
                            pump.getName(),
                            pump.getActive(),
                            nozzleConfigs
                        );
                    })
                    .collect(Collectors.toList());

                return new StationDTO.IslandFullConfig(
                    island.getId(),
                    island.getName(),
                    island.getStatus().name(),
                    island.getPosition(),
                    pumpConfigs
                );
            })
            .collect(Collectors.toList());

        return new StationDTO.StationFullConfigResponse(
            station.getId(),
            station.getName(),
            station.getAddress(),
            station.getPhone(),
            station.getActive(),
            islandConfigs
        );
    }
}
